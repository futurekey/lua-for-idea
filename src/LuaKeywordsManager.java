/*
 * Copyright 2009 Max Ishchenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sylvanaar.idea.Lua;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.util.Range;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 17.07.2009
 * Time: 16:35:47
 */
public class LuaKeywordsManager implements ApplicationComponent {

    //anything can happen inside these directive context
    public static final Set<String> CHAOS_DIRECTIVES = new HashSet<String>();

    // e.g. server -> ["NGX_MAIL_SRV_CONF", "NGX_HTTP_SRV_CONF"] etc
    // used to check that a directive can reside in context
    // though checks should be more smart, distinguishing between "server in http" and "server in mail"
    public static final ContextToFlagMapping CONTEXT_TO_FLAG = new ContextToFlagMapping();

    //used for detecting right number of arguments
    public static final Map<String, Range<Integer>> FLAG_TO_RANGE = new HashMap<String, Range<Integer>>();
    public static final Range<Integer> RANGE_FOR_UNKNOWN_DIRECTIVE = new Range<Integer>(0, Integer.MAX_VALUE);


    static {
        CHAOS_DIRECTIVES.add("types");
        CHAOS_DIRECTIVES.add("charset_map");
        CHAOS_DIRECTIVES.add("map");

        CONTEXT_TO_FLAG.add("http", "NGX_HTTP_MAIN_CONF");
        CONTEXT_TO_FLAG.add("location", "NGX_HTTP_LOC_CONF");
        CONTEXT_TO_FLAG.add("server", "NGX_HTTP_SRV_CONF", "NGX_MAIL_SRV_CONF");
        CONTEXT_TO_FLAG.add("limit_except", "NGX_HTTP_LMT_CONF");
        CONTEXT_TO_FLAG.add("if", "NGX_HTTP_SIF_CONF", "NGX_HTTP_LIF_CONF");
        CONTEXT_TO_FLAG.add("upstream", "NGX_HTTP_UPS_CONF");
        CONTEXT_TO_FLAG.add("mail", "NGX_MAIL_MAIN_CONF");
        CONTEXT_TO_FLAG.add("events", "NGX_EVENT_CONF");

        FLAG_TO_RANGE.put("NGX_CONF_FLAG", new Range<Integer>(1, 1));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE1", new Range<Integer>(1, 1));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE23", new Range<Integer>(2, 3));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE12", new Range<Integer>(1, 2));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE13", new Range<Integer>(1, 3));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE123", new Range<Integer>(1, 3));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE1234", new Range<Integer>(1, 4));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE3", new Range<Integer>(3, 3));
        FLAG_TO_RANGE.put("NGX_CONF_TAKE2", new Range<Integer>(2, 2));
        FLAG_TO_RANGE.put("NGX_CONF_2MORE", new Range<Integer>(2, Integer.MAX_VALUE));
        FLAG_TO_RANGE.put("NGX_CONF_1MORE", new Range<Integer>(1, Integer.MAX_VALUE));
        FLAG_TO_RANGE.put("NGX_CONF_NOARGS", new Range<Integer>(0, 0));

    }

    // keyword -> flags
    private Map<String, Set<String>> keywords = new HashMap<String, Set<String>>();

    // inner variables like $host
    private Set<String> variables = new HashSet<String>();

    private static Pattern COMPLEX_VARIABLES_PATTERN = Pattern.compile("(?:(?:arg)|(?:http)|(?:cookie)|(?:upstream_http))_\\w+");

    //keyword -> [flags, flags, ...]
    private Map<String, List<Set<String>>> ambiguousKeywords = new HashMap<String, List<Set<String>>>();
    private final String ANY_CONTEXT_FLAG = "NGX_ANY_CONF";

    public void initComponent() {

        BufferedReader keywordsReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("keywords.txt")));
        BufferedReader variablesReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("variables.txt")));
        Exception oops = null;
        try {
            readKeywords(keywordsReader);
            readVariables(variablesReader);
        } catch (IOException e) {
            oops = e;
        } finally {
            try {
                keywordsReader.close();
            } catch (IOException e) {
                oops = e;
            }
            try {
                variablesReader.close();
            } catch (IOException e) {
                oops = e;
            }
        }
        if (oops != null) {
            throw new RuntimeException(oops);
        }

    }

    public Set<String> getKeywords() {
        return keywords.keySet();
    }

    public Set<String> getVariables() {
        return variables;
    }

    public boolean checkBooleanKeyword(String directive) {
        return doCheckFlag(directive, "NGX_CONF_FLAG");
    }


    public boolean checkCanHaveChildContext(String directive) {
        Set<String> flags = keywords.get(directive);
        return flags == null || flags.contains("NGX_CONF_BLOCK"); //true if directive not found

    }

    public Range<Integer> getValueRange(String directive) {
        Set<String> rangeFlags = FLAG_TO_RANGE.keySet();
        Set<String> flags = keywords.get(directive);
        for (String flag : flags) {
            if (rangeFlags.contains(flag)) {
                return FLAG_TO_RANGE.get(flag);
            }
        }
        return RANGE_FOR_UNKNOWN_DIRECTIVE; //is that ever possible?
    }

    public boolean checkCanResideInMainContext(String string) {
        return doCheckFlag(string, "NGX_MAIN_CONF") || doCheckFlag(string, ANY_CONTEXT_FLAG);
    }

    public boolean checkCanHaveParentContext(String directive, String context) {

        //checking if directive can reside anywhere
        if (doCheckFlag(directive, ANY_CONTEXT_FLAG)) {
            return true;
        }

        Set<String> flagsForContext = CONTEXT_TO_FLAG.getFlagsFor(context);

        if (flagsForContext == null) {
            return true; //checkCanHaveChildContext will tell the truth when called on parent
        } else {
            boolean yesWeCan = false;
            for (String flag : flagsForContext) {
                yesWeCan = yesWeCan || doCheckFlag(directive, flag);
            }
            return yesWeCan;
        }

    }

    /**
     * @return server -> [location, listen, ...], http -> [server, allow, ...], ...
     */
    public Map<String, Set<String>> getContextToDirectiveListMappings() {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        for (Map.Entry<String, Set<String>> item : CONTEXT_TO_FLAG.map.entrySet()) {
            String context = item.getKey();
            Set<String> directivesForContext = getDirectivesThatCanResideIn(context);
            result.put(context, directivesForContext);
        }
        return result;
    }

    /**
     * @param context the context autocompletion is called in
     * @return possible directives for given context
     */
    public Set<String> getDirectivesThatCanResideIn(String context) {

        Set<String> flags = CONTEXT_TO_FLAG.getFlagsFor(context);
        if (flags == null) { //unknown parent - let's allow for any directove
            return keywords.keySet();
        }
        Set<String> result = new HashSet<String>();
        for (Map.Entry<String, Set<String>> entry : keywords.entrySet()) {
            for (String flag : flags) {
                if (entry.getValue().contains(flag) || entry.getValue().contains(ANY_CONTEXT_FLAG)) {
                    result.add(entry.getKey());
                }
            }
        }
        return result;

    }

    public Set<String> getDirectivesThatCanResideInMainContext() {
        Set<String> result = new HashSet<String>();
        for (Map.Entry<String, Set<String>> entry : keywords.entrySet()) {
            if (checkCanResideInMainContext(entry.getKey())) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public boolean isValidInnerVariable(String name) {
        return variables.contains(name) || COMPLEX_VARIABLES_PATTERN.matcher(name).matches();
    }

    private boolean doCheckFlag(String directive, String flag) {
        Set<String> flags = keywords.get(directive);
        return flags != null && flags.contains(flag);
    }

    private void readKeywords(BufferedReader reader) throws IOException {

        String line;
        while ((line = reader.readLine()) != null) {

            String[] splitLine = line.split(" ");

            String keyword = splitLine[0];
            Set<String> flags = new HashSet<String>();
            flags.addAll(Arrays.asList(splitLine).subList(1, splitLine.length));
            if (!keywords.containsKey(keyword)) {
                keywords.put(keyword, flags);
            } else {
                //todo: deal with ambiguous
            }

        }
    }

    private void readVariables(BufferedReader variablesReader) throws IOException {

        String line;
        while ((line = variablesReader.readLine()) != null) {
            variables.add(line);
        }

    }


    public void disposeComponent() {

    }

    @NotNull
    public String getComponentName() {
        return "Lua.keywords";
    }


    private static class ContextToFlagMapping {

        Map<String, Set<String>> map = new HashMap<String, Set<String>>();

        void add(String context, String... flags) {
            Set<String> set = new HashSet<String>();
            set.addAll(Arrays.asList(flags));
            map.put(context, set);
        }

        Set<String> getFlagsFor(String context) {
            return map.get(context);
        }

    }

}


