package digital.pragmatech;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
public class TranscriptionService {

  private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);

  private final OpenAiAudioTranscriptionModel openAiAudioApi;

  public TranscriptionService(OpenAiAudioTranscriptionModel openAiAudioApi) {
    this.openAiAudioApi = openAiAudioApi;
  }

  public String transcribeAudio(byte[] audioData) {
    Assert.notNull(audioData, "Audio data must not be null");
    Assert.isTrue(audioData.length > 0, "Audio data must not be empty");

    log.info("Received audio data for transcription ({} bytes)", audioData.length);
    // Use a unique name or timestamp if needed, but "audio.wav" is sufficient here
    // The actual format might depend on how the browser records it (e.g., webm, ogg)
    // Whisper is generally good at auto-detecting.
    ByteArrayResource audioResource = new ByteArrayResource(audioData) {
      @Override
      public String getFilename() {
        return "audio.webm";
      }
    };

    OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
      .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
      .build();

    AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(audioResource, options);

    try {
      log.info("Sending audio data to transcription service...");

      AudioTranscriptionResponse response = openAiAudioApi.call(prompt);
      if (response != null && response.getResult() != null) {
        String transcription = response.getResult().getOutput();
        log.debug("Transcription result: {}", transcription);
        return transcription;
      } else {
        log.warn("Received null response or result from transcription service.");
        return "Error: Transcription failed (No result)";
      }
    } catch (Exception e) {
      log.error("Error during audio transcription", e);
      return "Error: Transcription failed (" + e.getMessage() + ")";
    }
  }
}
