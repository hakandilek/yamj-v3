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
package org.yamj.core.database.model;

import org.apache.commons.lang3.StringUtils;

import org.yamj.core.database.model.type.JobType;
import org.yamj.core.hibernate.usertypes.EnumStringUserType;
import java.io.Serializable;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.*;
import org.hibernate.annotations.Parameter;

@TypeDef(name = "jobType",
        typeClass = EnumStringUserType.class,
        parameters = {
    @Parameter(name = "enumClassName", value = "org.yamj.core.database.model.type.JobType")})
@Entity
@Table(name = "cast_crew")
public class CastCrew extends AbstractIdentifiable implements Serializable {

    private static final long serialVersionUID = -3941301942248344131L;

    @NaturalId
    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "FK_CASTCREW_PERSON")
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @NaturalId
    @ManyToOne(fetch = FetchType.LAZY)
    @ForeignKey(name = "FK_CASTCREW_VIDEODATA")
    @Fetch(FetchMode.SELECT)
    @JoinColumn(name = "videodata_id", nullable = false, insertable = false, updatable = false)
    private VideoData videoData;

    @NaturalId
    @Type(type = "jobType")
    @Column(name = "job", nullable = false, length = 30)
    private JobType jobType;

    @Column(name = "role", length = 255)
    private String role;

    // GETTER and SETTER

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public VideoData getVideoData() {
        return videoData;
    }

    public void setVideoData(VideoData videoData) {
        this.videoData = videoData;
    }

    public JobType getJobType() {
        return jobType;
    }

    private void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public String getRole() {
        return role;
    }

    private void setRole(String role) {
        this.role = role;
    }

    public boolean setJob(JobType jobType, String role) {
        setJobType(jobType);
        if ((JobType.ACTOR.equals(jobType) || JobType.GUEST_STAR.equals(jobType))
            && StringUtils.isNotBlank(role)
            && !StringUtils.equals(getRole(), role))
        {
            setRole(role);
            return true;
        }
        return false;
    }
}
