package com.example.k8s.hello.backend;

import java.util.concurrent.ForkJoinPool;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
class BackendController {

	@GetMapping("/compute")
	DeferredResult<ComputationResult> computeStuff() {
		DeferredResult<ComputationResult> output = new DeferredResult<>();
		ForkJoinPool.commonPool().submit(() -> {
			try {
				Thread.sleep(6000);
			}
			catch (InterruptedException ignored) {
			}
			output.setResult(new ComputationResult(42));
		});
		return output;
	}

	static class ComputationResult {

		private final long value;

		public ComputationResult(long value) {
			this.value = value;
		}

		public long getValue() {
			return this.value;
		}
	}

}
