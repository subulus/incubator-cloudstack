// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package org.apache.cloudstack.storage.datastore.driver;

import java.util.List;
import java.util.Set;

import org.apache.cloudstack.engine.subsystem.api.storage.CommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.CopyCommandResult;
import org.apache.cloudstack.engine.subsystem.api.storage.CreateCmdResult;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStream;
import org.apache.cloudstack.engine.subsystem.api.storage.DataStore;
import org.apache.cloudstack.engine.subsystem.api.storage.EndPoint;
import org.apache.cloudstack.framework.async.AsyncCallbackDispatcher;
import org.apache.cloudstack.framework.async.AsyncCompletionCallback;
import org.apache.cloudstack.framework.async.AsyncRpcConext;
import org.apache.cloudstack.storage.command.CreateVolumeAnswer;
import org.apache.cloudstack.storage.command.CreateVolumeCommand;
import org.apache.cloudstack.storage.command.DeleteCommand;
import org.apache.cloudstack.storage.datastore.PrimaryDataStore;
import org.apache.cloudstack.storage.snapshot.SnapshotInfo;
import org.apache.cloudstack.storage.volume.PrimaryDataStoreDriver;
import org.apache.log4j.Logger;

import com.cloud.agent.api.Answer;


public class DefaultPrimaryDataStoreDriverImpl implements PrimaryDataStoreDriver {
    private static final Logger s_logger = Logger.getLogger(DefaultPrimaryDataStoreDriverImpl.class);
    protected PrimaryDataStore dataStore;
    public DefaultPrimaryDataStoreDriverImpl(PrimaryDataStore dataStore) {
        this.dataStore = dataStore;
    }
    
    public DefaultPrimaryDataStoreDriverImpl() {
        
    }
    
    private class CreateVolumeContext<T> extends AsyncRpcConext<T> {
        private final DataStream volume;
        /**
         * @param callback
         */
        public CreateVolumeContext(AsyncCompletionCallback<T> callback, DataStream volume) {
            super(callback);
            this.volume = volume;
        }
        
        public DataStream getVolume() {
            return this.volume;
        }
        
    }
    
    public Void createAsyncCallback(AsyncCallbackDispatcher<DefaultPrimaryDataStoreDriverImpl, Answer> callback, CreateVolumeContext<CommandResult> context) {
        CommandResult result = new CommandResult();
        CreateVolumeAnswer volAnswer = (CreateVolumeAnswer) callback.getResult();
        if (volAnswer.getResult()) {
            DataStream volume = context.getVolume();
            //volume.setPath(volAnswer.getVolumeUuid());
        } else {
            result.setResult(volAnswer.getDetails());
        }
        
        context.getParentCallback().complete(result);
        return null;
    }
  
    @Override
    public void deleteAsync(DataStream vo, AsyncCompletionCallback<CommandResult> callback) {
        DeleteCommand cmd = new DeleteCommand(vo.getUri());
        List<EndPoint> endPoints = null;
        EndPoint ep = endPoints.get(0);
        AsyncRpcConext<CommandResult> context = new AsyncRpcConext<CommandResult>(callback);
        AsyncCallbackDispatcher<DefaultPrimaryDataStoreDriverImpl, Answer> caller = AsyncCallbackDispatcher.create(this);
        caller.setCallback(caller.getTarget().deleteCallback(null, null))
            .setContext(context);
        ep.sendMessageAsync(cmd, caller);
    }
    
    public Void deleteCallback(AsyncCallbackDispatcher<DefaultPrimaryDataStoreDriverImpl, Answer> callback, AsyncRpcConext<CommandResult> context) {
        CommandResult result = new CommandResult();
        Answer answer = callback.getResult();
        if (!answer.getResult()) {
            result.setResult(answer.getDetails());
        }
        context.getParentCallback().complete(result);
        return null;
    }
    /*
    private class CreateVolumeFromBaseImageContext<T> extends AsyncRpcConext<T> {
        private final VolumeObject volume;
      
        public CreateVolumeFromBaseImageContext(AsyncCompletionCallback<T> callback, VolumeObject volume) {
            super(callback);
            this.volume = volume;
        }
        
        public VolumeObject getVolume() {
            return this.volume;
        }
        
    }

    @Override
    public void createVolumeFromBaseImageAsync(VolumeObject volume, TemplateInfo template, AsyncCompletionCallback<CommandResult> callback) {
        VolumeTO vol = this.dataStore.getVolumeTO(volume);
        List<EndPoint> endPoints = this.dataStore.getEndPoints();
        EndPoint ep = endPoints.get(0);
        String templateUri = template.getDataStore().grantAccess(template, ep);
        CreateVolumeFromBaseImageCommand cmd = new CreateVolumeFromBaseImageCommand(vol, templateUri);
        
        CreateVolumeFromBaseImageContext<CommandResult> context = new CreateVolumeFromBaseImageContext<CommandResult>(callback, volume);
        AsyncCallbackDispatcher<DefaultPrimaryDataStoreDriverImpl, Answer> caller = AsyncCallbackDispatcher.create(this);
        caller.setContext(context)
            .setCallback(caller.getTarget().createVolumeFromBaseImageAsyncCallback(null, null));

        ep.sendMessageAsync(cmd, caller);
    }*/
    /*
    public Object createVolumeFromBaseImageAsyncCallback(AsyncCallbackDispatcher<DefaultPrimaryDataStoreDriverImpl, Answer> callback, CreateVolumeFromBaseImageContext<CommandResult> context) {
        CreateVolumeAnswer answer = (CreateVolumeAnswer)callback.getResult();
        CommandResult result = new CommandResult();
        if (answer == null || answer.getDetails() != null) {
            result.setSucess(false);
            if (answer != null) {
                result.setResult(answer.getDetails());
            }
        } else {
            result.setSucess(true);
            VolumeObject volume = context.getVolume();
            volume.setPath(answer.getVolumeUuid());
        }
        AsyncCompletionCallback<CommandResult> parentCall = context.getParentCallback();
        parentCall.complete(result);
        return null;
    }*/

    @Override
    public void createAsync(DataStream vol,
            AsyncCompletionCallback<CreateCmdResult> callback) {
        List<EndPoint> endPoints = null;
        EndPoint ep = endPoints.get(0);
        CreateVolumeCommand createCmd = new CreateVolumeCommand(vol.getUri());
        
        CreateVolumeContext<CommandResult> context = null;
        AsyncCallbackDispatcher<DefaultPrimaryDataStoreDriverImpl, Answer> caller = AsyncCallbackDispatcher.create(this);
        caller.setContext(context)
            .setCallback(caller.getTarget().createAsyncCallback(null, null));

        ep.sendMessageAsync(createCmd, caller);
        
    }

    @Override
    public String grantAccess(DataStream vol, EndPoint ep) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean revokeAccess(DataStream vol, EndPoint ep) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Set<DataStream> listObjects(DataStore store) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void takeSnapshot(SnapshotInfo snapshot,
            AsyncCompletionCallback<CommandResult> callback) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void revertSnapshot(SnapshotInfo snapshot,
            AsyncCompletionCallback<CommandResult> callback) {
        // TODO Auto-generated method stub
        
    }

    

    @Override
    public boolean canCopy(DataStream srcData, DataStream destData) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void copyAsync(DataStream srcdata, DataStream destData,
            AsyncCompletionCallback<CopyCommandResult> callback) {
        // TODO Auto-generated method stub
        
    }
   
}