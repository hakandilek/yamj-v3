/*
 *      Copyright (c) 2004-2015 YAMJ Members
 *      https://github.com/organizations/YAMJ/teams
 *
 *      This file is part of the Yet Another Media Jukebox (YAMJ).
 *
 *      YAMJ is free software: you can redistribute it and/or modify
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
 *      along with YAMJ.  If not, see <http://www.gnu.org/licenses/>.
 *
 *      Web: https://github.com/YAMJ/yamj-v3
 *
 */
package org.yamj.core.api.json;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.yamj.core.api.model.ApiStatus;
import org.yamj.core.api.model.builder.DataItem;
import org.yamj.core.api.model.dto.ApiEpisodeDTO;
import org.yamj.core.api.model.dto.ApiExternalIdDTO;
import org.yamj.core.api.model.dto.ApiSeriesInfoDTO;
import org.yamj.core.api.model.dto.ApiVideoDTO;
import org.yamj.core.api.options.OptionsEpisode;
import org.yamj.core.api.options.OptionsExternalId;
import org.yamj.core.api.options.OptionsIdArtwork;
import org.yamj.core.api.options.OptionsIndexVideo;
import org.yamj.core.api.wrapper.ApiWrapperList;
import org.yamj.core.api.wrapper.ApiWrapperSingle;
import org.yamj.core.database.service.JsonApiStorageService;

@Controller
@ResponseBody
@RequestMapping(value = "/api/video/**", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
public class VideoController {

    private static final Logger LOG = LoggerFactory.getLogger(VideoController.class);
    @Autowired
    private JsonApiStorageService jsonApi;

    /**
     * Get information on a movie
     *
     * TODO: Allow genres to be added to the returned data
     *
     * @param options
     * @return
     */
    @RequestMapping("/movie/{id}")
    public ApiWrapperSingle<ApiVideoDTO> getVideoById(@ModelAttribute("options") OptionsIndexVideo options) {
        ApiWrapperSingle<ApiVideoDTO> wrapper = new ApiWrapperSingle<>();
        // Set the type to movie
        options.setType("MOVIE");
        wrapper.setOptions(options);

        if (options.getId() > 0L) {
            LOG.info("Getting video with ID '{}'", options.getId());
            jsonApi.getSingleVideo(wrapper);
        }
        wrapper.setStatusCheck();
        return wrapper;
    }

    /**
     * Get information on a series
     *
     * TODO: Get associate seasons for the series
     *
     * @param options
     * @return
     */
    @RequestMapping("/series/{id}")
    public ApiWrapperSingle<ApiVideoDTO> getSeriesById(@ModelAttribute("options") OptionsIndexVideo options) {
        ApiWrapperSingle<ApiVideoDTO> wrapper = new ApiWrapperSingle<>();
        // Set the type to movie
        options.setType("SERIES");
        wrapper.setOptions(options);

        if (options.getId() > 0L) {
            LOG.info("Getting series with ID '{}'", options.getId());
            jsonApi.getSingleVideo(wrapper);
        }
        wrapper.setStatusCheck();
        return wrapper;
    }

    /**
     * Get information on a season
     *
     * TODO: Add episodes to the season
     *
     * @param options
     * @return
     */
    @RequestMapping("/season/{id}")
    public ApiWrapperSingle<ApiVideoDTO> getSeasonById(@ModelAttribute("options") OptionsIndexVideo options) {
        ApiWrapperSingle<ApiVideoDTO> wrapper = new ApiWrapperSingle<>();
        // Set the type to movie
        options.setType("SEASON");
        wrapper.setOptions(options);

        if (options.getId() > 0L) {
            LOG.info("Getting season with ID '{}'", options.getId());
            jsonApi.getSingleVideo(wrapper);
        }
        wrapper.setStatusCheck();
        return wrapper;
    }

    /**
     * Get information on a series
     *
     * @param options
     * @return
     */
    @RequestMapping("/seriesinfo")
    public ApiWrapperList<ApiSeriesInfoDTO> getSeriesInfo(@ModelAttribute("options") OptionsIdArtwork options) {
        ApiWrapperList<ApiSeriesInfoDTO> wrapper = new ApiWrapperList<>();
        wrapper.setOptions(options);

        if (options.getId() > 0L) {
            LOG.info("Getting series info for SeriesID '{}'", options.getId());
            if (options.hasDataItem(DataItem.ARTWORK) && StringUtils.isBlank(options.getArtwork())) {
                options.setArtwork("all");
            }
            jsonApi.getSeriesInfo(wrapper);
            wrapper.setStatusCheck();
        } else {
            wrapper.setStatusInvalidId();
        }
        return wrapper;
    }

    /**
     * Get information on episodes
     *
     * @param options
     * @return
     */
    @RequestMapping("/episodes")
    public ApiWrapperList<ApiEpisodeDTO> getEpisodes(@ModelAttribute("options") OptionsEpisode options) {
        LOG.info("Getting episodes for seriesId '{}', seasonId '{}', season '{}'",
                options.getSeriesid() < 0L ? "All" : options.getSeriesid(),
                options.getSeasonid() < 0L ? "All" : options.getSeasonid(),
                options.getSeason() < 0L ? "All" : options.getSeason());

        ApiWrapperList<ApiEpisodeDTO> wrapper = new ApiWrapperList<>();
        wrapper.setOptions(options);
        jsonApi.getEpisodeList(wrapper);
        wrapper.setStatusCheck();
        return wrapper;
    }

    /**
     * Get the list of external IDs for a video
     *
     * @param id
     * @param sourcedb
     * @param externalid
     * @return
     */
    @RequestMapping("/externalids")
    public ApiWrapperList<ApiExternalIdDTO> getExternalIds(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String sourcedb,
            @RequestParam(required = false) String externalid) {
        LOG.info("Getting External IDs for VideoData ID: {}, SourceDb: {}, ExternalID: {}",
                id, sourcedb, externalid);

        OptionsExternalId options = new OptionsExternalId();
        options.setId(id);
        options.setSourcedb(sourcedb);
        options.setExternalid(externalid);

        ApiWrapperList<ApiExternalIdDTO> wrapper = new ApiWrapperList<>();
        wrapper.setOptions(options);
        jsonApi.getExternalIds(wrapper);
        wrapper.setStatusCheck();
        return wrapper;
    }

    /**
     * Add or update an external id to the video
     *
     * @param id
     * @param sourcedb
     * @param externalid
     * @return
     */
    @RequestMapping("/externalids/update")
    public ApiStatus updateExternalId(
            @RequestParam(required = true) Long id,
            @RequestParam(required = true) String sourcedb,
            @RequestParam(required = true) String externalid
    ) {
        LOG.info("Add/Update Source: '{}' ExternalID: '{}' to ID: {}", sourcedb, externalid, id);
        return jsonApi.updateExternalId(id, sourcedb, externalid);
    }

    /**
     * Delete one or all of the external IDs from the video
     *
     * @param id
     * @param sourcedb
     * @return
     */
    @RequestMapping("/externalids/delete")
    public ApiStatus deleteExternalId(
            @RequestParam(required = true) Long id,
            @RequestParam(required = false, defaultValue = "") String sourcedb
    ) {
        LOG.info("Deleting Source: '{}' from ID: {}", sourcedb, id);
        return jsonApi.deleteExternalId(id, sourcedb);
    }

}
