/*
 * Copyright 2010 Jon S Akhtar (Sylvanaar)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.sylvanaar.idea.Lua.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Jon S Akhtar
 * Date: Apr 21, 2010
 * Time: 12:44:22 AM
 */
public class LuaRunner extends DefaultProgramRunner {
    @NotNull
    @Override
    public String getRunnerId() {
        return "com.sylvanaar.idea.Lua.run.LuaRunner";  
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
         return executorId.equals(DefaultRunExecutor.EXECUTOR_ID) && profile instanceof LuaRunConfiguration;  
    }

    @Override
    protected RunContentDescriptor doExecute(final Project project, final Executor executor, final RunProfileState state, final RunContentDescriptor contentToReuse,
                                           final ExecutionEnvironment env) throws ExecutionException {
        FileDocumentManager.getInstance().saveAllDocuments();
        ExecutionResult executionResult = state.execute(executor, this);
       // if (executionResult == null) return null;

        final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
        contentBuilder.setExecutionResult(executionResult);
        contentBuilder.setEnvironment(env);
        return contentBuilder.showRunContent(contentToReuse);
    }
}
