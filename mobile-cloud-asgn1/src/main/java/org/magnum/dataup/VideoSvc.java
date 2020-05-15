package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class VideoSvc {
	private static final AtomicLong currentId = new AtomicLong(0L);
	private Map<Long, Video> videos = new HashMap<>();

	public List<Video> getVideoList() {
		return new ArrayList<>(videos.values());
	}

	public Video saveVideoMetadata(Video video) {
		checkAndSetId(video);
		video.setDataUrl(getDataUrl(video.getId()));
		videos.put(video.getId(), video);
		return video;
	}

	public Video getVideo(Long id) {
		return videos.get(id);
	}

	private void checkAndSetId(Video video) {
		if (video.getId() == 0) {
			video.setId(currentId.incrementAndGet());
		}
	}

	private String getDataUrl(long videoId) {
		final HttpServletRequest request =
				((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		final String urlBase = "http://" + request.getServerName() +
				((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
		return urlBase + "/video/" + videoId + "/data";
	}
}
