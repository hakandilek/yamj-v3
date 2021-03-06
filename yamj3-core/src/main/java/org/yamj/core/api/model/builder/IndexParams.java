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
package org.yamj.core.api.model.builder;

import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.yamj.common.type.MetaDataType;
import org.yamj.core.api.options.OptionsIndexVideo;

/**
 * @author modmax
 */
public class IndexParams {

    private static final String YEAR = "year";
    private static final String GENRE = "genre";
    private static final String STUDIO = "studio";
    private static final String COUNTRY = "country";
    private static final String AWARD = "award";
    private static final String CERTIFICATION = "certification";
    private static final String VIDEOSOURCE = "videosource";
    private static final String RATING = "rating";
    private static final String NEWEST = "newest";
    private static final String BOXSET = "boxset";

    private final OptionsIndexVideo options;
    private final Map<String, String> includes;
    private final Map<String, String> excludes;
    private final List<DataItem> dataItems;
    private final Map<String, Object> parameters = new HashMap<>();

    private int certificationId = -1;
    private String year;
    private String genreName;
    private String studioName;
    private String countryName;
    private String awardName;
    private String videoSource;
    private String ratingSource;
    private int ratingValue;
    private String newestSource;
    private Date newestDate;
    private int boxSetId = -1;

    public IndexParams(OptionsIndexVideo options) {
        this.options = options;
        this.includes = options.splitIncludes();
        this.excludes = options.splitExcludes();
        this.dataItems = options.splitDataItems();
    }

    public List<MetaDataType> getMetaDataTypes() {
        return options.splitTypes();
    }

    public Long getId() {
        return options.getId();
    }

    public Boolean getWatched() {
        return options.getWatched();
    }

    public String getSearchString(boolean addWhere) {
        return options.getSearchString(addWhere);
    }

    public String getSortString() {
        return options.getSortString();
    }

    public List<DataItem> getDataItems() {
        return dataItems;
    }

    public boolean hasDataItem(DataItem dataItem) {
        return dataItems.contains(dataItem);
    }

    // year check
    public boolean includeYear() {
        return includes.containsKey(YEAR);
    }

    public boolean excludeYear() {
        return excludes.containsKey(YEAR);
    }

    public String getYear() {
        if (year == null) {
            if (includeYear()) {
                year = includes.get(YEAR);
            } else {
                year = excludes.get(YEAR);
            }
        }
        return year;
    }

    // genre check
    public boolean includeGenre() {
        return includes.containsKey(GENRE);
    }

    public boolean excludeGenre() {
        return excludes.containsKey(GENRE);
    }

    public String getGenreName() {
        if (genreName == null) {
            if (includeGenre()) {
                genreName = includes.get(GENRE);
            } else {
                genreName = excludes.get(GENRE);
            }
        }
        return genreName;
    }

    // studio check
    public boolean includeStudio() {
        return includes.containsKey(STUDIO);
    }

    public boolean excludeStudio() {
        return excludes.containsKey(STUDIO);
    }

    public String getStudioName() {
        if (studioName == null) {
            if (includeStudio()) {
                studioName = includes.get(STUDIO);
            } else {
                studioName = excludes.get(STUDIO);
            }
        }
        return studioName;
    }

    // country check
    public boolean includeCountry() {
        return includes.containsKey(COUNTRY);
    }

    public boolean excludeCountry() {
        return excludes.containsKey(COUNTRY);
    }

    public String getCountryName() {
        if (countryName == null) {
            if (includeCountry()) {
                countryName = includes.get(COUNTRY);
            } else {
                countryName = excludes.get(COUNTRY);
            }
        }
        return countryName;
    }

    // award check
    public boolean includeAward() {
        return includes.containsKey(AWARD);
    }

    public boolean excludeAward() {
        return excludes.containsKey(AWARD);
    }

    public String getAwardName() {
        if (awardName == null) {
            if (includeAward()) {
                awardName = includes.get(AWARD);
            } else {
                awardName = excludes.get(AWARD);
            }
        }
        return awardName;
    }
    
    // certification check
    public boolean includeCertification() {
        return includes.containsKey(CERTIFICATION);
    }

    public boolean excludeCertification() {
        return excludes.containsKey(CERTIFICATION);
    }

    public int getCertificationId() {
        if (certificationId == -1) {
            if (includeCertification()) {
                certificationId = NumberUtils.toInt(includes.get(CERTIFICATION), -1);
            } else {
                certificationId = NumberUtils.toInt(excludes.get(CERTIFICATION), -1);
            }
        }
        return certificationId;
    }

    // video source check
    public boolean includeVideoSource() {
        return includes.containsKey(VIDEOSOURCE);
    }

    public boolean excludeVideoSource() {
        return excludes.containsKey(VIDEOSOURCE);
    }

    public String getVideoSource() {
        if (videoSource == null) {
            if (includeVideoSource()) {
                videoSource = includes.get(VIDEOSOURCE);
            } else {
                videoSource = excludes.get(VIDEOSOURCE);
            }
        }
        return videoSource;
    }

    // video source check
    public boolean includeBoxedSet() {
        return includes.containsKey(BOXSET);
    }

    public boolean excludeBoxedSet() {
        return excludes.containsKey(BOXSET);
    }

    public int getBoxSetId() {
        if (boxSetId < 0) {
            if (includeBoxedSet()) {
                boxSetId = NumberUtils.toInt(includes.get(BOXSET), -1);
            } else {
                boxSetId = NumberUtils.toInt(excludes.get(BOXSET), -1);
            }
        }
        return boxSetId;
    }

    // rating check
    public boolean includeRating() {
        return includes.containsKey(RATING);
    }

    public boolean excludeRating() {
        return excludes.containsKey(RATING);
    }

    public String getRatingSource() {
        if (ratingSource == null) {
            if (includeRating()) {
                this.parseRating(includes.get(RATING));
            } else {
                this.parseRating(excludes.get(RATING));
            }
        }
        return ratingSource;
    }

    public int getRating() {
        return this.ratingValue;
    }

    private void parseRating(final String value) {
        String[] result = StringUtils.split(value, '-');
        if (result == null || result.length == 0) {
            return;
        }

        ratingValue = NumberUtils.toInt(result[0]);

        if (ratingValue < 0) {
            ratingValue = 0;
        } else if (ratingValue > 10) {
            ratingValue = 10;
        }

        if (result.length > 1) {
            ratingSource = result[1];
        } else {
            ratingSource = "combined";
        }
    }

    // newest check
    public boolean includeNewest() {
        return includes.containsKey(NEWEST);
    }

    public boolean excludeNewest() {
        return excludes.containsKey(NEWEST);
    }

    public String getNewestSource() {
        if (newestSource == null) {
            if (includeNewest()) {
                this.parseNewest(includes.get(NEWEST));
            } else {
                this.parseNewest(excludes.get(NEWEST));
            }
        }
        return newestSource;
    }

    public Date getNewestDate() {
        return newestDate;
    }

    private void parseNewest(final String value) {
        String[] result = StringUtils.split(value, '-');
        if (result == null || result.length == 0) {
            return;
        }

        // Set the default to 30 if the conversion fails
        int maxDays = NumberUtils.toInt(result[0], 30);
        if (maxDays < 0) {
            maxDays = 30;
        }

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -maxDays);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        newestDate = cal.getTime();

        if (result.length > 1) {
            this.newestSource = result[1];
        } else {
            this.newestSource = "file";
        }
    }

    public void addParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    public void addScalarParameters(SqlScalars sqlScalars) {
        for (Entry<String, Object> entry : parameters.entrySet()) {
            sqlScalars.addParameter(entry.getKey(), entry.getValue());
        }
    }
}
