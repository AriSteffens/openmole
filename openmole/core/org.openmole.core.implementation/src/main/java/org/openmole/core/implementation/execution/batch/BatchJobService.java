/*
 *  Copyright (C) 2010 reuillon
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.openmole.core.implementation.execution.batch;


import org.openmole.commons.exception.UserBadDataError;
import org.openmole.core.batchservicecontrol.UsageControl;
import org.openmole.core.batchservicecontrol.FailureControl;
import org.openmole.commons.exception.InternalProcessingError;
import org.openmole.core.implementation.internal.Activator;
import org.openmole.core.model.execution.batch.IAccessToken;
import org.openmole.core.model.execution.batch.IBatchEnvironment;
import org.openmole.core.model.execution.batch.IBatchJob;
import org.openmole.core.model.execution.batch.IBatchJobService;
import org.openmole.core.model.execution.batch.IBatchServiceAuthentication;
import org.openmole.core.model.execution.batch.IBatchServiceAuthenticationKey;
import org.openmole.core.model.execution.batch.IBatchServiceDescription;
import org.openmole.core.model.execution.batch.IRuntime;
import org.openmole.core.model.file.IURIFile;

public abstract class BatchJobService<ENV extends IBatchEnvironment, AUTH extends IBatchServiceAuthentication> extends BatchService<ENV, AUTH> implements IBatchJobService<ENV, AUTH> {

    public BatchJobService(ENV batchEnvironment, IBatchServiceAuthenticationKey<? extends AUTH> key, AUTH authentication, IBatchServiceDescription description, int nbAccess) throws InternalProcessingError, UserBadDataError, InterruptedException {
        super(batchEnvironment, key, authentication, description, new UsageControl(nbAccess), new FailureControl());
    }

    @Override
    public IBatchJob submit(IURIFile inputFile, IURIFile outputFile, IRuntime runtime, IAccessToken token) throws InternalProcessingError, UserBadDataError, InterruptedException {
        try {
            IBatchJob ret = doSubmit(inputFile, outputFile, runtime, token);
            Activator.getBatchRessourceControl().getController(getDescription()).getFailureControl().success();
            return ret;
        } catch (InternalProcessingError e) {
            Activator.getBatchRessourceControl().getController(getDescription()).getFailureControl().failed();
            throw e;
        }
    }
 
    protected abstract IBatchJob doSubmit(IURIFile inputFile, IURIFile outputFile, IRuntime runtime, IAccessToken token) throws InternalProcessingError, UserBadDataError, InterruptedException;
    
}
