package org.magnum.mobilecloud.video;

import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import retrofit.http.Path;

import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.Collection;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.magnum.mobilecloud.video.client.VideoSvcApi.DURATION_PARAMETER;
import static org.magnum.mobilecloud.video.client.VideoSvcApi.TITLE_PARAMETER;
import static org.magnum.mobilecloud.video.client.VideoSvcApi.VIDEO_DURATION_SEARCH_PATH;
import static org.magnum.mobilecloud.video.client.VideoSvcApi.VIDEO_SVC_PATH;
import static org.magnum.mobilecloud.video.client.VideoSvcApi.VIDEO_TITLE_SEARCH_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class VideoController {
	@Autowired
	private VideoRepository repo;

	@RequestMapping(value = VIDEO_SVC_PATH, method = GET)
	public @ResponseBody List<Video> getVideoList() {
		return (List<Video>) repo.findAll();
	}

	@RequestMapping(value = VIDEO_SVC_PATH, method = POST)
	public @ResponseBody Video addVideo(@RequestBody Video video,
				   HttpServletResponse response) {
		video.setLikes(0);
		video.getLikedBy().clear();
		final Video saved = repo.save(video);
		response.setStatus(SC_OK);
		response.setHeader("Content-Type", "application/json");
		return saved;
	}

	@RequestMapping(value = VIDEO_SVC_PATH + "/{id}", method = GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id,
					   HttpServletResponse response) {
		final Video video = repo.findOne(id);
		if (video == null) {
			response.setStatus(SC_NOT_FOUND);
		} else {
			response.setStatus(SC_OK);
		}
		return video;
	}

	@RequestMapping(value = VIDEO_SVC_PATH + "/{id}/like", method = POST)
	public void likeVideo(@PathVariable("id") long id,
						  HttpServletResponse response,
						  Principal principal) {
		final String userName = principal.getName();
		final Video video = repo.findOne(id);
		if (video == null) {
			response.setStatus(SC_NOT_FOUND);
		} else if (video.getLikedBy().contains(userName)) {
			response.setStatus(SC_BAD_REQUEST);
		} else {
			video.setLikes(video.getLikes() + 1);
			video.getLikedBy().add(userName);
			repo.save(video);
			response.setStatus(SC_OK);
		}
	}

	@RequestMapping(value = VIDEO_SVC_PATH + "/{id}/unlike", method = POST)
	public void unlikeVideo(@PathVariable("id") long id,
							HttpServletResponse response,
							Principal principal) {
		final String userName = principal.getName();
		final Video video = repo.findOne(id);
		if (video == null) {
			response.setStatus(SC_NOT_FOUND);
		} else if (!video.getLikedBy().contains(userName)) {
			response.setStatus(SC_BAD_REQUEST);
		} else {
			video.setLikes(video.getLikes() - 1);
			video.getLikedBy().remove(userName);
			repo.save(video);
			response.setStatus(SC_OK);
		}
	}

	@RequestMapping(value = VIDEO_TITLE_SEARCH_PATH, method = GET)
	public @ResponseBody Collection<Video> findByTitle(@RequestParam(TITLE_PARAMETER) String title) {
		return repo.findByName(title);
	}

	@RequestMapping(value = VIDEO_DURATION_SEARCH_PATH, method = GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(@RequestParam(DURATION_PARAMETER) long duration) {
		return repo.findByDurationLessThan(duration);
	}
}
