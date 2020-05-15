package org.magnum.dataup;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.magnum.dataup.VideoSvcApi.DATA_PARAMETER;
import static org.magnum.dataup.VideoSvcApi.VIDEO_DATA_PATH;
import static org.magnum.dataup.VideoSvcApi.VIDEO_SVC_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class VideoController {
	private VideoSvc videoSvc;
	private VideoFileManager videoFileManager;

	public VideoController() throws IOException {
		videoSvc = new VideoSvc();
		videoFileManager = VideoFileManager.get();
	}

	@RequestMapping(value = VIDEO_SVC_PATH, method = GET)
	public @ResponseBody List<Video> getVideoList() {
		return videoSvc.getVideoList();
	}

	@RequestMapping(value = VIDEO_SVC_PATH, method = POST)
	public @ResponseBody Video addVideoMetadata(@RequestBody Video video,
												HttpServletResponse response) {
		Video saved = videoSvc.saveVideoMetadata(video);
		response.setStatus(SC_OK);
		response.setHeader("Content-Type", "application/json");
		return saved;
	}

	@RequestMapping(value = VIDEO_DATA_PATH, method = GET)
	public HttpServletResponse getVideoData(@PathVariable Long id,
											HttpServletResponse response) throws IOException {
		Video video = videoSvc.getVideo(id);
		if (video == null || !videoFileManager.hasVideoData(video)) {
			response.setStatus(HttpStatus.NOT_FOUND.value());
		} else {
			videoFileManager.copyVideoData(video, response.getOutputStream());
			response.setStatus(SC_OK);
			response.setHeader("Content-Type", "video/mp4");
		}
		return response;
	}

	@RequestMapping(value = VIDEO_DATA_PATH, method = POST)
	public @ResponseBody VideoStatus addVideoBinaryData(@PathVariable Long id,
								   @RequestParam(DATA_PARAMETER) MultipartFile videoData,
								   HttpServletResponse response) throws IOException {
		Video video = videoSvc.getVideo(id);
		response.setHeader("Content-Type", "application/json");
		if (video == null) {
			response.resetBuffer();
			response.setStatus(SC_NOT_FOUND);
			response.flushBuffer();
		} else {
			videoFileManager.saveVideoData(video, videoData.getInputStream());
			response.setStatus(SC_OK);
		}
		return new VideoStatus(VideoStatus.VideoState.READY);
	}
}
