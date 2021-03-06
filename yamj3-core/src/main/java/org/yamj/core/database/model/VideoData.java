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
package org.yamj.core.database.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.MapKeyType;
import org.hibernate.annotations.Type;
import org.yamj.common.type.StatusType;
import org.yamj.core.database.model.award.MovieAward;
import org.yamj.core.database.model.dto.AwardDTO;
import org.yamj.core.database.model.dto.CreditDTO;
import org.yamj.core.database.model.type.ArtworkType;
import org.yamj.core.database.model.type.OverrideFlag;

@Entity
@Table(name = "videodata",
        uniqueConstraints = @UniqueConstraint(name = "UIX_VIDEODATA_NATURALID", columnNames = {"identifier"}),
        indexes = {
            @Index(name = "IX_VIDEODATA_TITLE", columnList = "title"),
            @Index(name = "IX_VIDEODATA_STATUS", columnList = "status"),
            @Index(name = "IX_VIDEODATA_PUBLICATIONYEAR", columnList = "publication_year")}
)
@SuppressWarnings({"unused", "deprecation"})
public class VideoData extends AbstractMetadata {

    private static final long serialVersionUID = 885531396557944590L;

    @Column(name = "episode", nullable = false)
    private int episode = -1;

    @Column(name = "publication_year", nullable = false)
    private int publicationYear = -1;

    @Temporal(value = TemporalType.DATE)
    @Column(name = "release_date")
    private Date releaseDate;

    @Column(name = "top_rank", nullable = false)
    private int topRank = -1;

    @Lob
    @Column(name = "tagline", length = 25000)
    private String tagline;

    @Lob
    @Column(name = "quote", length = 25000)
    private String quote;

    @Column(name = "skip_online_scans", length = 255)
    private String skipOnlineScans;

    @Column(name = "watched_nfo", nullable = false)
    private boolean watchedNfo = false;

    @Column(name = "watched_file", nullable = false)
    private boolean watchedFile = false;

    @Column(name = "watched_api", nullable = false)
    private boolean watchedApi = false;

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "videodata_ids",
            joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_VIDEODATA_SOURCEIDS"), name = "videodata_id"))
    @Fetch(FetchMode.SELECT)
    @MapKeyColumn(name = "sourcedb", length = 40)
    @Column(name = "sourcedb_id", length = 200, nullable = false)
    private Map<String, String> sourceDbIdMap = new HashMap<>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "videodata_ratings",
            joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_VIDEODATA_RATINGS"), name = "videodata_id"))
    @Fetch(FetchMode.SELECT)
    @MapKeyColumn(name = "sourcedb", length = 40)
    @Column(name = "rating", nullable = false)
    private Map<String, Integer> ratings = new HashMap<>(0);

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "videodata_override",
            joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_VIDEODATA_OVERRIDE"), name = "videodata_id"))
    @Fetch(FetchMode.SELECT)
    @MapKeyColumn(name = "flag", length = 30)
    @MapKeyType(value = @Type(type = "overrideFlag"))
    @Column(name = "source", length = 30, nullable = false)
    private Map<OverrideFlag, String> overrideFlags = new EnumMap<>(OverrideFlag.class);

    @ManyToMany
    @JoinTable(name = "videodata_genres",
            joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATAGENRES_VIDEODATA"), name = "data_id"),
            inverseJoinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATAGENRES_GENRE"), name = "genre_id"))
    private Set<Genre> genres = new HashSet<>(0);

    @ManyToMany
    @JoinTable(name = "videodata_studios",
            joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATASTUDIOS_VIDEODATA"), name = "data_id"),
            inverseJoinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATASTUDIOS_STUDIO"), name = "studio_id"))
    private Set<Studio> studios = new HashSet<>(0);

    @ManyToMany
    @JoinTable(name = "videodata_countries",
            joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATACOUNTRIES_VIDEODATA"), name = "data_id"),
            inverseJoinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATACOUNTRIES_COUNTRY"), name = "country_id"))
    private Set<Country> countries = new HashSet<>(0);

    @ManyToMany
    @JoinTable(name = "videodata_certifications",
            joinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATACERTS_VIDEODATA"), name = "data_id"),
            inverseJoinColumns = @JoinColumn(foreignKey = @ForeignKey(name = "FK_DATACERTS_CERTIFICATION"), name = "cert_id"))
    private Set<Certification> certifications = new HashSet<>(0);

    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_VIDEODATA_SEASON"), name = "season_id")
    private Season season;

    @ManyToMany(mappedBy = "videoDatas")
    private Set<MediaFile> mediaFiles = new HashSet<>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "castCrewPK.videoData")
    private List<CastCrew> credits = new ArrayList<>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "videoData")
    private List<BoxedSetOrder> boxedSets = new ArrayList<>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "videoData")
    private List<Artwork> artworks = new ArrayList<>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "nfoRelationPK.videoData")
    private List<NfoRelation> nfoRelations = new ArrayList<>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "movieAwardPK.videoData")
    private List<MovieAward> movieAwards = new ArrayList<>(0);

    @Transient
    private Set<String> genreNames;

    @Transient
    private Set<String> studioNames;

    @Transient
    private Set<String> countryNames;

    @Transient
    private final Map<String, String> certificationInfos = new HashMap<>(0);

    @Transient
    private final Map<String, Integer> setInfos = new HashMap<>(0);

    @Transient
    private final Set<CreditDTO> creditDTOS = new LinkedHashSet<>(0);

    @Transient
    private final Map<String, String> posterURLS = new HashMap<>(0);

    @Transient
    private final Map<String, String> fanartURLS = new HashMap<>(0);

    @Transient
    private final Set<AwardDTO> awardDTOS = new HashSet<>(0);

    // CONSTRUCTORS
    public VideoData() {
        super();
    }

    public VideoData(String identifier) {
        super();
        setIdentifier(identifier);
    }

    // GETTER and SETTER
    public int getPublicationYear() {
        return publicationYear;
    }

    private void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setPublicationYear(int publicationYear, String source) {
        if (publicationYear > 0) {
            this.publicationYear = publicationYear;
            setOverrideFlag(OverrideFlag.YEAR, source);
        }
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    private void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setReleaseDate(Date releaseDate, String source) {
        if (releaseDate != null) {
            this.releaseDate = releaseDate;
            setOverrideFlag(OverrideFlag.RELEASEDATE, source);
        }
    }

    public int getTopRank() {
        return topRank;
    }

    public void setTopRank(int topRank) {
        if (topRank > 0) {
            this.topRank = topRank;
        }
    }

    public String getTagline() {
        return tagline;
    }

    private void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public void setTagline(String tagline, String source) {
        if (StringUtils.isNotBlank(tagline)) {
            this.tagline = tagline.trim();
            setOverrideFlag(OverrideFlag.TAGLINE, source);
        }
    }

    public String getQuote() {
        return quote;
    }

    private void setQuote(String quote) {
        this.quote = quote;
    }

    public void setQuote(String quote, String source) {
        if (StringUtils.isNotBlank(quote)) {
            this.quote = quote.trim();
            setOverrideFlag(OverrideFlag.QUOTE, source);
        }
    }

    public Map<String, String> getSourceDbIdMap() {
        return sourceDbIdMap;
    }

    @Override
    public String getSourceDbId(String sourceDb) {
        return sourceDbIdMap.get(sourceDb);
    }

    public void setSourceDbIdMap(Map<String, String> sourceDbIdMap) {
        this.sourceDbIdMap = sourceDbIdMap;
    }

    @Override
    public String getSkipOnlineScans() {
        return skipOnlineScans;
    }

    public void setSkipOnlineScans(String skipOnlineScans) {
        this.skipOnlineScans = skipOnlineScans;
    }

    public boolean isWatchedNfo() {
        return watchedNfo;
    }

    public void setWatchedNfo(boolean watchedNfo) {
        this.watchedNfo = watchedNfo;
    }

    public boolean isWatchedFile() {
        return watchedFile;
    }

    public void setWatchedFile(boolean watchedFile) {
        this.watchedFile = watchedFile;
    }

    public boolean isWatchedApi() {
        return watchedApi;
    }

    public void setWatchedApi(boolean watchedApi) {
        this.watchedApi = watchedApi;
    }

    @Override
    public boolean setSourceDbId(String sourceDb, String id) {
        if (StringUtils.isBlank(sourceDb) || StringUtils.isBlank(id)) {
            return false;
        }
        String newId = id.trim();
        String oldId = this.sourceDbIdMap.put(sourceDb, newId);
        return (!StringUtils.equals(oldId, newId));
    }

    private Map<String, Integer> getRatings() {
        return ratings;
    }

    private void setRatings(Map<String, Integer> ratings) {
        this.ratings = ratings;
    }

    public void addRating(String sourceDb, int rating) {
        if (StringUtils.isNotBlank(sourceDb) && (rating >= 0)) {
            this.ratings.put(sourceDb, rating);
        }
    }

    private Map<OverrideFlag, String> getOverrideFlags() {
        return overrideFlags;
    }

    private void setOverrideFlags(Map<OverrideFlag, String> overrideFlags) {
        this.overrideFlags = overrideFlags;
    }

    @Override
    public void setOverrideFlag(OverrideFlag overrideFlag, String source) {
        this.overrideFlags.put(overrideFlag, source.toLowerCase());
    }

    @Override
    public String getOverrideSource(OverrideFlag overrideFlag) {
        return overrideFlags.get(overrideFlag);
    }

    @Override
    public boolean removeOverrideSource(final String source) {
        boolean removed = false;
        for (Iterator<Entry<OverrideFlag, String>> it = this.overrideFlags.entrySet().iterator(); it.hasNext();) {
            Entry<OverrideFlag, String> e = it.next();
            if (StringUtils.endsWithIgnoreCase(e.getValue(), source)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public Set<Studio> getStudios() {
        return studios;
    }

    public void setStudios(Set<Studio> studios) {
        this.studios = studios;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public void setCountries(Set<Country> countries) {
        this.countries = countries;
    }

    public Set<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(Set<Certification> certifications) {
        this.certifications = certifications;
    }

    public Season getSeason() {
        return season;
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public Set<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    private void setMediaFiles(Set<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    public void addMediaFile(MediaFile mediaFile) {
        this.mediaFiles.add(mediaFile);
    }

    public List<CastCrew> getCredits() {
        return credits;
    }

    private void setCredits(List<CastCrew> credits) {
        this.credits = credits;
    }

    public List<Artwork> getArtworks() {
        return artworks;
    }

    public Artwork getArtwork(ArtworkType artworkType) {
        for (Artwork artwork : getArtworks()) {
            if (artworkType.equals(artwork.getArtworkType())) {
                return artwork;
            }
        }
        return null;
    }

    public void setArtworks(List<Artwork> artworks) {
        this.artworks = artworks;
    }

    public List<BoxedSetOrder> getBoxedSets() {
        return boxedSets;
    }

    private void setBoxedSets(List<BoxedSetOrder> boxedSets) {
        this.boxedSets = boxedSets;
    }

    public void addBoxedSet(BoxedSetOrder boxedSet) {
        this.boxedSets.add(boxedSet);
    }

    public List<NfoRelation> getNfoRelations() {
        return nfoRelations;
    }

    public void setNfoRelations(List<NfoRelation> nfoRelations) {
        this.nfoRelations = nfoRelations;
    }

    public void addNfoRelation(NfoRelation nfoRelation) {
        this.nfoRelations.add(nfoRelation);
    }

    public List<MovieAward> getMovieAwards() {
        return movieAwards;
    }

    public void setMovieAwards(List<MovieAward> movieAwards) {
        this.movieAwards = movieAwards;
    }

    // TRANSIENTS METHODS
    public boolean isWatched() {
        return (this.watchedNfo || this.watchedFile || this.watchedApi);
    }

    public Set<CreditDTO> getCreditDTOS() {
        return creditDTOS;
    }

    public void addCreditDTO(CreditDTO creditDTO) {
        this.creditDTOS.add(creditDTO);
    }

    public void addCreditDTOS(Set<CreditDTO> creditDTOS) {
        this.creditDTOS.addAll(creditDTOS);
    }

    public Set<String> getGenreNames() {
        return genreNames;
    }

    public void setGenreNames(Set<String> genreNames, String source) {
        if (CollectionUtils.isNotEmpty(genreNames)) {
            this.genreNames = genreNames;
            setOverrideFlag(OverrideFlag.GENRES, source);
        }
    }

    public Set<String> getStudioNames() {
        return studioNames;
    }

    public void setStudioNames(Set<String> studioNames, String source) {
        if (CollectionUtils.isNotEmpty(studioNames)) {
            this.studioNames = studioNames;
            setOverrideFlag(OverrideFlag.STUDIOS, source);
        }
    }

    public Set<String> getCountryNames() {
        return countryNames;
    }

    public void setCountryNames(Set<String> countryNames, String source) {
        if (CollectionUtils.isNotEmpty(countryNames)) {
            this.countryNames = countryNames;
            setOverrideFlag(OverrideFlag.COUNTRIES, source);
        }
    }

    public Map<String, String> getCertificationInfos() {
        return certificationInfos;
    }

    public void setCertificationInfos(Map<String, String> certificationInfos) {
        if (MapUtils.isNotEmpty(certificationInfos)) {
            for (Entry<String, String> entry : certificationInfos.entrySet()) {
                this.addCertificationInfo(entry.getKey(), entry.getValue());
            }
        }
    }

    public void addCertificationInfo(String country, String certificate) {
        if (StringUtils.isNotBlank(country) && StringUtils.isNotBlank(certificate)) {
            // check if country already present
            for (String stored : this.certificationInfos.keySet()) {
                if (country.equalsIgnoreCase(stored)) {
                    // certificate for country already present
                    return;
                }
            }
            this.certificationInfos.put(country, certificate);
        }
    }

    public Map<String, Integer> getSetInfos() {
        return setInfos;
    }

    public void addSetInfo(String setName, Integer ordering) {
        if (StringUtils.isNotBlank(setName)) {
            this.setInfos.put(setName, ordering);
        }
    }

    public void addSetInfos(Map<String, Integer> setInfos) {
        if (MapUtils.isNotEmpty(setInfos)) {
            this.setInfos.putAll(setInfos);
        }
    }

    public Map<String, String> getPosterURLS() {
        return posterURLS;
    }

    public void addPosterURL(String posterURL, String source) {
        if (StringUtils.isNotBlank(posterURL) && StringUtils.isNotBlank(source)) {
            this.posterURLS.put(posterURL.trim(), source);
        }
    }

    public Map<String, String> getFanartURLS() {
        return fanartURLS;
    }

    public void addFanartURL(String fanartURL, String source) {
        if (StringUtils.isNotBlank(fanartURL) && StringUtils.isNotBlank(source)) {
            this.fanartURLS.put(fanartURL.trim(), source);
        }
    }

    public Set<AwardDTO> getAwardDTOS() {
        return awardDTOS;
    }

    public void addAwards(Collection<AwardDTO> awards, String source) {
        if (CollectionUtils.isEmpty(awards)) {
            return;
        }

        for (AwardDTO award : awards) {
            if (StringUtils.isBlank(award.getEvent()) || StringUtils.isBlank(award.getCategory()) || award.getYear() <= 0) {
                // event, category and year must be given
                continue;
            }
            award.setSource(source);
            this.awardDTOS.add(award);
        }
    }

    public void addAward(String event, String category, int year, String source) {
        if (StringUtils.isNotBlank(event) && StringUtils.isNotBlank(category) && year > 0 && StringUtils.isNotBlank(source)) {
            this.awardDTOS.add(new AwardDTO(event, category, year, source).setWon(true));
        }
    }

    // TV CHECKS
    public boolean isTvEpisodeDone(String sourceDb) {
        if (StringUtils.isBlank(this.getSourceDbId(sourceDb))) {
            // not done if episode ID not set
            return false;
        }
        return (StatusType.DONE.equals(this.getStatus()));
    }

    public void setTvEpisodeDone() {
        super.setLastScanned(new Date(System.currentTimeMillis()));
        this.setStatus(StatusType.TEMP_DONE);
    }

    public void setTvEpisodeNotFound() {
        super.setLastScanned(new Date(System.currentTimeMillis()));
        if (StatusType.DONE.equals(this.getStatus())) {
            // do not reset done
            return;
        } else if (StatusType.TEMP_DONE.equals(this.getStatus())) {
            // do not reset temporary done
            return;
        }
        this.setStatus(StatusType.NOTFOUND);
    }

    @Override
    public int getEpisodeNumber() {
        return episode;
    }

    @Override
    public boolean isMovie() {
        return (episode < 0);
    }

    // EQUALITY CHECKS
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getIdentifier())
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VideoData)) {
            return false;
        }
        final VideoData other = (VideoData) obj;
        // first check the id
        if ((getId() > 0) && (other.getId() > 0)) {
            return getId() == other.getId();
        }
        // check other values
        return new EqualsBuilder()
                .append(getIdentifier(), other.getIdentifier())
                .isEquals();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("VideoData [ID=");
        sb.append(getId());
        sb.append(", identifier=");
        sb.append(getIdentifier());
        sb.append(", title=");
        sb.append(getTitle());
        sb.append(", year=");
        sb.append(getPublicationYear());
        sb.append("]");
        return sb.toString();
    }
}
