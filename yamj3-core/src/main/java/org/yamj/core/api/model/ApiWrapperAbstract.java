/*
 *      Copyright (c) 2004-2013 YAMJ Members
 *      https://github.com/organizations/YAMJ/teams
 *
 *      This file is part of the Yet Another Media Jukebox (YAMJ).
 *
 *      The YAMJ is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      YAMJ is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with the YAMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 *      Web: https://github.com/YAMJ/yamj-v3
 *
 */
package org.yamj.core.api.model;

import org.joda.time.DateTime;
import org.yamj.common.tools.DateTimeTools;

/**
 *
 * @author Stuart
 */
public abstract class ApiWrapperAbstract {

    private int count = 0;
    private DateTime queryTime = DateTime.now();
    private ApiStatus status = new ApiStatus();
    private Parameters parameters = new Parameters();

    public int getCount() {
        return count;
    }

    public String getQueryTime() {
        return DateTimeTools.convertDateToString(queryTime, DateTimeTools.BUILD_FORMAT);
    }

    public ApiStatus getStatus() {
        return status;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setQueryTime(DateTime queryTime) {
        this.queryTime = queryTime;
    }

    public void setStatus(ApiStatus status) {
        this.status = status;
    }

    /**
     * Shorthand method to create a default "OK" status or "FAIL" status
     */
    public abstract void setStatusCheck();

    protected void setCount(int count) {
        this.count = count;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
}