/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.learningspringboot;

import java.io.IOException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Greg Turnquist
 */
@Controller
public class HomeController {

	private static final String BASE_PATH = "/images";
	private static final String FILENAME = "{filename:.+}";

	private final ImageService imageService;

	public HomeController(ImageService imageService) {
		this.imageService = imageService;
	}

	@GetMapping("/")
	public Mono<String> index(Model model) {
		model.addAttribute("images",
			imageService.findAllImages());
		return Mono.just("index");
	}

	@GetMapping(BASE_PATH + "/" + FILENAME + "/raw")
	@ResponseBody
	public Mono<ResponseEntity<?>> oneRawImage(
		@PathVariable String filename) {
		// tag::try-catch[]
		return imageService.findOneImage(filename)
			.map(resource -> {
				try {
					return ResponseEntity.ok()
						.contentLength(resource.contentLength())
						.contentType(MediaType.IMAGE_JPEG)
						.body(new InputStreamResource(
							resource.getInputStream()));
				} catch (IOException e) {
					return ResponseEntity.badRequest()
						.body("Couldn't find " + filename +
							" => " + e.getMessage());
				}
			});
		// end::try-catch[]
	}

	@PostMapping(value = BASE_PATH)
	public Mono<String> createFile(@RequestPart(name = "file")
										   Flux<FilePart> files) {
		return imageService.createImage(files)
			.map(aVoid -> "redirect:/");
	}

	// TODO: Drop "/delete" suffix when https://jira.spring.io/browse/SPR-15291 is patched in Spring 5.0 RC2
	@DeleteMapping(BASE_PATH + "/" + FILENAME + "/delete")
	public Mono<String> deleteFile(@PathVariable String filename) {
		return imageService.deleteImage(filename)
			.map(aVoid -> "redirect:/");
	}

}