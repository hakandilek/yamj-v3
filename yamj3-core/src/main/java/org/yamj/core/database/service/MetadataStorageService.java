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
package org.yamj.core.database.service;

import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.yamj.common.type.StatusType;
import org.yamj.core.database.dao.CommonDao;
import org.yamj.core.database.dao.MetadataDao;
import org.yamj.core.database.model.*;
import org.yamj.core.database.model.dto.CreditDTO;
import org.yamj.core.database.model.dto.QueueDTO;
import org.yamj.core.database.model.type.MetaDataType;

@Service("metadataStorageService")
public class MetadataStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(MetadataStorageService.class);

    @Autowired
    private CommonDao commonDao;
    @Autowired
    private MetadataDao metadataDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public void save(Object entity) {
        this.commonDao.saveEntity(entity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void update(Object entity) {
        this.commonDao.updateEntity(entity);
    }

    @Transactional(readOnly = true)
    public List<QueueDTO> getMediaQueueForScanning(final int maxResults) {
        final StringBuilder sql = new StringBuilder();
        sql.append("select vd.id,'");
        sql.append(MetaDataType.VIDEODATA);
        sql.append("' as mediatype,vd.create_timestamp,vd.update_timestamp ");
        sql.append("from videodata vd ");
        sql.append("where vd.status in ('NEW','UPDATED') ");
        sql.append("and vd.episode<0 ");
        sql.append("union ");
        sql.append("select ser.id,'");
        sql.append(MetaDataType.SERIES);
        sql.append("' as mediatype,ser.create_timestamp,ser.update_timestamp ");
        sql.append("from series ser, season sea, videodata vd ");
        sql.append("where ser.id=sea.series_id ");
        sql.append("and sea.id=vd.season_id ");
        sql.append("and (ser.status in ('NEW','UPDATED') ");
        sql.append(" or  sea.status in ('NEW','UPDATED') ");
        sql.append(" or  vd.status in  ('NEW','UPDATED')) ");

        return metadataDao.getMetadataQueue(sql, maxResults);
    }
    
    @Transactional(readOnly = true)
    public List<QueueDTO> getPersonQueueForScanning(final int maxResults) {
        final StringBuilder sql = new StringBuilder();
        sql.append("select id, '");
        sql.append(MetaDataType.PERSON);
        sql.append("' as mediatype, create_timestamp, update_timestamp ");
        sql.append("from person ");
        sql.append("where status in ('");
        sql.append(StatusType.NEW);
        sql.append("','");
        sql.append(StatusType.UPDATED);
        sql.append("') ");

        return metadataDao.getMetadataQueue(sql, maxResults);
    }

    @Transactional(readOnly = true)
    public VideoData getRequiredVideoData(Long id) {
        final StringBuilder sb = new StringBuilder();
        sb.append("from VideoData vd ");
        sb.append("left outer join fetch vd.genres ");
        sb.append("left outer join fetch vd.credits c ");
        sb.append("where vd.id = ?" );

        @SuppressWarnings("unchecked")
        List<VideoData> objects = this.commonDao.getObjectsById(sb, id);
        return DataAccessUtils.requiredUniqueResult(objects);
    }

    @Transactional(readOnly = true)
    public Series getRequiredSeries(Long id) {
        final StringBuilder sb = new StringBuilder();
        sb.append("from Series ser ");
        sb.append("join fetch ser.seasons sea ");
        sb.append("join fetch sea.videoDatas vd ");
        sb.append("left outer join fetch vd.credits c ");
        sb.append("where ser.id = ?" );

        @SuppressWarnings("unchecked")
        List<Series> objects = this.commonDao.getObjectsById(sb, id);
        return DataAccessUtils.requiredUniqueResult(objects);
    }

    @Transactional(readOnly = true)
    public Person getRequiredPerson(Long id) {
        final StringBuilder sb = new StringBuilder();
        sb.append("from Person person where person.id = ?");

        // later on there it could be necessary to fetch associated entities

        @SuppressWarnings("unchecked")
        List<Person> objects = this.commonDao.getObjectsById(sb, id);
        return DataAccessUtils.requiredUniqueResult(objects);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void store(VideoData videoData) {
        // update entity
        metadataDao.updateEntity(videoData);

        // update genres
        updateGenres(videoData);

        // update cast and crew
        updateCastCrew(videoData);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void store(Series series) {
        // update entity
        metadataDao.updateEntity(series);

        // update underlying seasons and episodes
        for (Season season : series.getSeasons()) {
            if (StatusType.PROCESSED.equals(season.getStatus())) {
                season.setStatus(StatusType.DONE);
                metadataDao.updateEntity(season);
            }
            for (VideoData videoData : season.getVideoDatas()) {
                if (StatusType.PROCESSED.equals(videoData.getStatus())) {
                    videoData.setStatus(StatusType.DONE);
                    store(videoData);
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void store(Person person) {
        // update entity
        metadataDao.updateEntity(person);
    }

    private void updateGenres(VideoData videoData) {
        HashSet<Genre> genres = new HashSet<Genre>(0);
        for (Genre genre : videoData.getGenres()) {
            Genre stored = commonDao.getGenre(genre.getName());
            if (stored == null) {
                commonDao.saveEntity(genre);
                genres.add(genre);
            } else {
                genres.add(stored);
            }
        }
        videoData.setGenres(genres);
    }

    private void updateCastCrew(VideoData videoData) {
        for (CreditDTO dto : videoData.getCreditDTOS()) {
            Person person = null;
            CastCrew castCrew = null;

            for (CastCrew credit : videoData.getCredits()) {
                if ((credit.getJobType() == dto.getJobType()) && StringUtils.equalsIgnoreCase(dto.getName(), credit.getPerson().getName())) {
                    castCrew = credit;
                    person = credit.getPerson();
                    break;
                }
            }

            // find person if not found
            if (person == null) {
                LOG.info("Attempting to retrieve information on '{}' from database", dto.getName());
                person = metadataDao.getPerson(dto.getName());
            } else {
                LOG.debug("Found '{}' in cast table", person.getName());
            }

            if (person != null) {
                // update person id
                if (person.setPersonId(dto.getSourcedb(), dto.getSourcedbId())) {
                    metadataDao.updateEntity(person);
                }
            } else {
                // create new person
                person = new Person();
                person.setName(dto.getName());
                person.setPersonId(dto.getSourcedb(), dto.getSourcedbId());
                person.setStatus(StatusType.NEW);
                metadataDao.saveEntity(person);
            }

            if (castCrew == null) {
                // create new association between person and video
                castCrew = new CastCrew();
                castCrew.setPerson(person);
                castCrew.setJob(dto.getJobType(), dto.getRole());
                castCrew.setVideoData(videoData);
                videoData.addCredit(castCrew);
                metadataDao.saveEntity(castCrew);
            } else if (castCrew.setJob(castCrew.getJobType(), dto.getRole())) {
                // updated role
                metadataDao.updateEntity(castCrew);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void errorVideoData(Long id) {
        VideoData videoData = metadataDao.getVideoData(id);
        if (videoData != null) {
            videoData.setStatus(StatusType.ERROR);
            metadataDao.updateEntity(videoData);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void errorSeries(Long id) {
        Series series = metadataDao.getSeries(id);
        if (series != null) {
            series.setStatus(StatusType.ERROR);
            metadataDao.updateEntity(series);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void errorPerson(Long id) {
        Person person = metadataDao.getPerson(id);
        if (person != null) {
            person.setStatus(StatusType.ERROR);
            metadataDao.updateEntity(person);
        }
    }
}