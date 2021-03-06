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

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.yamj.core.api.model.ApiStatus;
import org.yamj.core.api.model.dto.ApiArtworkDTO;
import org.yamj.core.api.options.OptionsId;
import org.yamj.core.api.options.OptionsIndexArtwork;
import org.yamj.core.api.wrapper.ApiWrapperList;
import org.yamj.core.api.wrapper.ApiWrapperSingle;
import org.yamj.core.database.service.CommonStorageService;
import org.yamj.core.database.service.JsonApiStorageService;
import org.yamj.core.service.file.FileStorageService;

@Controller
@ResponseBody
@RequestMapping(value = "/api/artwork/**", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
public class ArtworkController {

    private static final Logger LOG = LoggerFactory.getLogger(ArtworkController.class);
    @Autowired
    private JsonApiStorageService api;
    @Autowired
    private CommonStorageService commonStorageService;
    @Autowired
    private FileStorageService fileStorageService;
    
    @RequestMapping("/{id}")
    public ApiWrapperSingle<ApiArtworkDTO> getArtwork(@PathVariable Long id) {
        ApiWrapperSingle<ApiArtworkDTO> wrapper = new ApiWrapperSingle<>();

        LOG.info("Attempting to retrieve artwork with id '{}'", id);
        ApiArtworkDTO artwork = api.getArtworkById(id);
        LOG.info("Artwork: {}", artwork);

        // Add the result to the wrapper
        wrapper.setResult(artwork);
        wrapper.setStatusCheck();

        return wrapper;
    }

    @RequestMapping("/list")
    public ApiWrapperList<ApiArtworkDTO> getArtworkList(@ModelAttribute("options") OptionsIndexArtwork options) {
        LOG.info("INDEX: Artwork list - Options: {}", options.toString());
        ApiWrapperList<ApiArtworkDTO> wrapper = new ApiWrapperList<>();
        wrapper.setOptions(options);
        wrapper.setResults(api.getArtworkList(wrapper));
        wrapper.setStatusCheck();

        return wrapper;
    }

    /**
     * Mark a located artwork as ignored.
     *
     * @param options
     * @return
     */
    @RequestMapping("/located/ignore/{id}")
    public ApiStatus deleteLocatedById(@ModelAttribute("options") OptionsId options) {
        ApiStatus status = new ApiStatus();
        Long id = options.getId();
        if (id != null && id > 0L) {
            LOG.info("Ignore located artwork '{}'", id);
            Set<String> filesToDelete = this.commonStorageService.ignoreArtworkLocated(id);
            if (filesToDelete != null) {
                this.fileStorageService.deleteStorageFiles(filesToDelete);
                status.setStatus(200);
                status.setMessage("Successfully marked located artwork '" + id + "' as ignored");
            } else {
                status.setStatus(400);
                status.setMessage("Located artwork not found '" + id + "'");
            }
        } else {
            status.setStatus(400);
            status.setMessage("Invalid located artwork id specified");
        }
        return status;
    }
}
