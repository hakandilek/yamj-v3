package com.yamj.core.service.artwork;

import com.yamj.common.type.StatusType;
import com.yamj.core.database.dao.ArtworkDao;
import com.yamj.core.database.model.Artwork;
import com.yamj.core.database.model.dto.QueueDTO;
import com.yamj.core.database.model.type.ArtworkType;
import com.yamj.core.service.artwork.fanart.IFanartScanner;
import com.yamj.core.service.artwork.fanart.IMovieFanartScanner;
import com.yamj.core.service.artwork.poster.IMoviePosterScanner;
import com.yamj.core.service.artwork.poster.IPosterScanner;
import java.util.HashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("artworkScannerService")
public class ArtworkScannerService {

    private static final Logger LOG = LoggerFactory.getLogger(ArtworkScannerService.class);
    
    @Autowired
    private ArtworkDao artworkDao;

    private HashMap<String, IMoviePosterScanner> registeredMoviePosterScanner = new HashMap<String, IMoviePosterScanner>();
    private HashMap<String, IMovieFanartScanner> registeredMovieFanartScanner = new HashMap<String, IMovieFanartScanner>();

    public void registerMoviePosterScanner(IMoviePosterScanner posterScanner) {
        registeredMoviePosterScanner.put(posterScanner.getScannerName().toLowerCase(), posterScanner);
    }

    public void registerMovieFanartScanner(IMovieFanartScanner fanartScanner) {
        registeredMovieFanartScanner.put(fanartScanner.getScannerName().toLowerCase(), fanartScanner);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void scanArtwork(QueueDTO queueElement) {
        if (queueElement == null) {
            // nothing to
            return;
        }

        Artwork artwork = artworkDao.getArtwork(queueElement.getId());
        if (artwork == null) {
            throw new RuntimeException("Found no artwork for id " + queueElement.getId());
        }
        
        if (ArtworkType.POSTER.equals(artwork.getArtworkType())) {
            boolean found = this.scanPosterLocal(artwork);
            if (!found) {
                this.scanPosterOnline(artwork);
            }
        } else if (ArtworkType.FANART.equals(artwork.getArtworkType())) {
            boolean found = this.scanFanartLocal(artwork);
            if (!found) {
                this.scanFanartOnline(artwork);
            }
        } else {
            throw new RuntimeException("No valid element for scanning artwork '"+queueElement+"'");
        }

        // update artwork in database
        if (artwork.getStageFile() == null && StringUtils.isBlank(artwork.getUrl())) {
            artwork.setStatus(StatusType.MISSING);
        } else {
            artwork.setStatus(StatusType.PROCESS);
        }
        artworkDao.updateEntity(artwork);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void processingError(QueueDTO queueElement) {
        if (queueElement == null) {
            // nothing to
            return;
        }

        Artwork artwork = artworkDao.getArtwork(queueElement.getId());
        if (artwork != null) {
            artwork.setStatus(StatusType.ERROR);
            artworkDao.updateEntity(artwork);
        }
    }

    private boolean scanPosterLocal(Artwork artwork) {
        LOG.trace("Scan local for poster: {}", artwork);
        
        // TODO local scan
        return false;
    }

    private void scanPosterOnline(Artwork artwork) {
        LOG.trace("Scan online for poster: {}", artwork);

        String posterUrl = null;

        if (artwork.getVideoData() != null) {
            // CASE: movie poster scan

            // TODO evaluate search priority
            for (IPosterScanner scanner : this.registeredMoviePosterScanner.values()) {
                posterUrl = scanner.getPosterUrl(artwork.getVideoData());
                if (StringUtils.isNotBlank(posterUrl)) {
                    artwork.setUrl(posterUrl);
                    break;
                }
            }
        } else {
            throw new RuntimeException("Artwork search not implemented for " + artwork);
        }
    }

    private boolean scanFanartLocal(Artwork artwork) {
        LOG.trace("Scan local for fanart: {}", artwork);
        
        // TODO local scan
        return false;
    }

    private void scanFanartOnline(Artwork artwork) {
        LOG.trace("Scan online for fanart: {}", artwork);

        String fanartUrl = null;

        if (artwork.getVideoData() != null) {
            // CASE: movie fanart scan

            // TODO evaluate search priority
            for (IFanartScanner scanner : this.registeredMovieFanartScanner.values()) {
                fanartUrl = scanner.getFanartUrl(artwork.getVideoData());
                if (StringUtils.isNotBlank(fanartUrl)) {
                    artwork.setUrl(fanartUrl);
                    break;
                }
            }
        } else {
            throw new RuntimeException("Artwork search not implemented for " + artwork);
        }
    }
}
