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

package com.sylvanaar.idea.Lua.platform;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.sylvanaar.idea.Lua.configurator.LuaServerDescriptor;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: Max
 * Date: 27.07.2009
 * Time: 1:32:19
 */
public class WindowsSpecificTools implements PlatformDependentTools {

    public static final String DEFAULT_CONF_PATH = "/conf/Lua.conf";
    public static final String DEFAULT_PID_PATH = "/logs/Lua.pid";
    public static final String DEFAULT_HTTP_LOG_PATH = "/logs/access.log";
    public static final String DEFAULT_ERROR_LOG_PATH = "/logs/error.log";

    public static final String[] EMPTY = new String[0];

    public static final String[] STOP_COMMAND = new String[]{"-s", "stop"};
    public static final String[] RELOAD_COMMAND = new String[]{"-s", "reload"};
    public static final String[] TEST_COMMAND = new String[]{"-t"};

    public boolean checkExecutable(VirtualFile file) {
        return file != null && !file.isDirectory() && StringUtil.endsWithIgnoreCase(file.getName(), ".exe");
    }

    public boolean checkExecutable(String path) {
        return checkExecutable(LocalFileSystem.getInstance().findFileByPath(path));
    }

    public String[] getStartCommand(LuaServerDescriptor descriptor) {
        String[] commandWithoutGlobals = new String[]{descriptor.getExecutablePath(), "-c", descriptor.getConfigPath()};
        String[] globals = getGlobals(descriptor);
        return ArrayUtil.mergeArrays(commandWithoutGlobals, globals, String.class);
    }

    public String[] getStopCommand(LuaServerDescriptor descriptor) {
        return ArrayUtil.mergeArrays(getStartCommand(descriptor), STOP_COMMAND, String.class);
    }

    public String[] getReloadCommand(LuaServerDescriptor descriptor) {
        return ArrayUtil.mergeArrays(getStartCommand(descriptor), RELOAD_COMMAND, String.class);
    }

    public String[] getTestCommand(LuaServerDescriptor descriptor) {
        return ArrayUtil.mergeArrays(getStartCommand(descriptor), TEST_COMMAND, String.class);
    }

    public LuaServerDescriptor createDescriptorFromFile(VirtualFile file) throws ThisIsNotLuaExecutableException {

        LuaCompileParameters compileParameters = LuaCompileParametersExtractor.extract(file);

        LuaServerDescriptor descriptor = getDefaultDescriptorFromFile(file);
        descriptor.setName("Lua/Windows [" + compileParameters.getVersion() + "]");

        String prefix;
        if (compileParameters.getPrefix() != null) {
            prefix = compileParameters.getPrefix();
            if (prefix.equals("")) {
                prefix = file.getParent().getPath();
            }
        } else {
            prefix = file.getParent().getPath(); // There is no default prefix for windows, so let it be current dir
        }

        descriptor.setConfigPath(getPrefixDependendSettings(compileParameters.getConfigurationPath(), prefix, DEFAULT_CONF_PATH));
        descriptor.setPidPath(getPrefixDependendSettings(compileParameters.getPidPath(), prefix, DEFAULT_PID_PATH));

        descriptor.setHttpLogPath(getPrefixDependendSettings(compileParameters.getHttpLogPath(), prefix, DEFAULT_HTTP_LOG_PATH));
        descriptor.setErrorLogPath(getPrefixDependendSettings(compileParameters.getErrorLogPath(), prefix, DEFAULT_ERROR_LOG_PATH));

        return descriptor;

    }

    public LuaServerDescriptor getDefaultDescriptorFromFile(VirtualFile virtualFile) {

        LuaServerDescriptor result = new LuaServerDescriptor();
        result.setName("Lua/Windows [unknown version]");
        result.setExecutablePath(virtualFile.getPath());
        result.setConfigPath(virtualFile.getParent().getPath() + DEFAULT_CONF_PATH);
        result.setPidPath(virtualFile.getParent().getPath() + DEFAULT_PID_PATH);
        result.setHttpLogPath(virtualFile.getParent().getPath() + DEFAULT_HTTP_LOG_PATH);
        result.setErrorLogPath(virtualFile.getParent().getPath() + DEFAULT_ERROR_LOG_PATH);
        return result;

    }

    private String getPrefixDependendSettings(String configurationPath, String prefix, String defaultValue) {

        //this logic is not needed for linux as default prefix exists there
        if (configurationPath != null) {
            if (new File(configurationPath).isAbsolute()) {
                return configurationPath;
            } else {
                String slash = "/";
                if (prefix.endsWith(slash) || configurationPath.startsWith(slash)) {
                    slash = "";
                }
                return prefix + slash + configurationPath;
            }
        } else {
            return prefix + defaultValue;
        }
    }


    private String[] getGlobals(LuaServerDescriptor descriptor) {
        String globals = "pid " + descriptor.getPidPath() + ";"; //we don't want Lua run as daemon process
        if (descriptor.getGlobals().length() > 0) {
            globals = globals + " " + descriptor.getGlobals();
        }
        return new String[]{"-g", globals};
    }

}
