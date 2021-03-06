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
package org.yamj.core.service.artwork;

import org.yamj.core.service.artwork.ArtworkTools.HashCodeType;

public class ArtworkDetailDTO {

    private final String source;
    private final String url;
    private final String hashCode;
    private String language = null;
    private int rating = -1;

    public ArtworkDetailDTO(String source, String url) {
        this(source, url, HashCodeType.SIMPLE);
    }

    public ArtworkDetailDTO(String source, String url, HashCodeType hashCodeType) {
        this.source = source;
        this.url = url;
        this.hashCode = ArtworkTools.getUrlHashCode(url, hashCodeType);
    }
    
    public String getSource() {
        return source;
    }

    public String getUrl() {
        return url;
    }

    public String getHashCode() {
        return hashCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ArtworkDetailDTO [Source=");
        sb.append(getSource());
        sb.append(", url=");
        sb.append(getUrl());
        sb.append(", hashCode=");
        sb.append(getHashCode());
        sb.append(", language=");
        sb.append(getLanguage());
        sb.append(", rating=");
        sb.append(getRating());
        sb.append("]");
        return sb.toString();
    }
}
