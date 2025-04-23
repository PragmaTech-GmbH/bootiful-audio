package digital.pragmatech;

import java.util.Base64;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Route("") // Map to the root path
@PageTitle("Speech to Text Demo")
@PermitAll
public class MainView extends VerticalLayout {

  private static final Logger log = LoggerFactory.getLogger(MainView.class);

  private final TranscriptionService transcriptionService;

  private Button startRecordButton;
  private Button stopRecordButton;
  private TextArea transcriptionArea;
  private Paragraph statusLabel;
  private ProgressBar progressBar;

  public MainView(TranscriptionService transcriptionService) {
    this.transcriptionService = transcriptionService;

    setupUI();
    setupRecorder(); // Setup JavaScript interaction
  }

  private void setupUI() {
    setAlignItems(Alignment.CENTER);
    setSizeFull();

    statusLabel = new Paragraph("Status: Idle");
    startRecordButton = new Button("Start Recording");
    stopRecordButton = new Button("Stop Recording");
    transcriptionArea = new TextArea("Transcription");
    progressBar = new ProgressBar();

    transcriptionArea.setWidth("80%");
    transcriptionArea.setHeight("200px");
    transcriptionArea.setReadOnly(true);
    stopRecordButton.setEnabled(false); // Disabled initially
    progressBar.setIndeterminate(true);
    progressBar.setVisible(false); // Hidden initially

    startRecordButton.addClickListener(e -> {
      startRecording();
      statusLabel.setText("Status: Recording...");
      startRecordButton.setEnabled(false);
      stopRecordButton.setEnabled(true);
      progressBar.setVisible(false); // Hide progress during recording
      transcriptionArea.setValue(""); // Clear previous transcription
    });

    stopRecordButton.addClickListener(e -> {
      stopRecording();
      statusLabel.setText("Status: Processing...");
      startRecordButton.setEnabled(false); // Keep disabled until processing done
      stopRecordButton.setEnabled(false);
      progressBar.setVisible(true); // Show progress during transcription
    });

    add(statusLabel, startRecordButton, stopRecordButton, progressBar, transcriptionArea);
  }

  // --- JavaScript Interaction ---

  @Override
  protected void onAttach(AttachEvent attachEvent) {
    super.onAttach(attachEvent);
    // Inject the JavaScript needed for recording once the component is attached
    setupRecorder();
  }

  private void setupRecorder() {
    // Inject JavaScript code into the browser
    // This JS code handles microphone access and recording
    UI.getCurrent().getPage().executeJs(
      "window.VaadinSpeechRecorder = {" +
        "  mediaRecorder: null," +
        "  audioChunks: []," +
        "  stream: null," +
        "  init: function(component) {" +
        "    window.VaadinSpeechRecorder.component = component;" +
        "  }," +
        "  start: async function() {" +
        "    try {" +
        "      window.VaadinSpeechRecorder.stream = await navigator.mediaDevices.getUserMedia({ audio: true });" +
        "      window.VaadinSpeechRecorder.audioChunks = [];" +
        "      window.VaadinSpeechRecorder.mediaRecorder = new MediaRecorder(window.VaadinSpeechRecorder.stream);" +
        "      window.VaadinSpeechRecorder.mediaRecorder.ondataavailable = event => {" +
        "        window.VaadinSpeechRecorder.audioChunks.push(event.data);" +
        "      };" +
        "      window.VaadinSpeechRecorder.mediaRecorder.onstop = () => {" +
        "        const audioBlob = new Blob(window.VaadinSpeechRecorder.audioChunks, { type: 'audio/webm' });" + // Adjust type if needed
        "        const reader = new FileReader();" +
        "        reader.onloadend = () => {" +
        "          const base64String = reader.result.split(',')[1];" + // Get Base64 part
        "          window.VaadinSpeechRecorder.component.$server.handleAudioData(base64String);" +
        "        };" +
        "        reader.readAsDataURL(audioBlob);" +
        "        window.VaadinSpeechRecorder.stream.getTracks().forEach(track => track.stop()); " + // Release microphone
        "        console.log('Recording stopped, sending data.');" +
        "      };" +
        "      window.VaadinSpeechRecorder.mediaRecorder.start();" +
        "      console.log('Recording started.');" +
        "    } catch (err) {" +
        "      console.error('Error accessing microphone:', err);" +
        "      window.VaadinSpeechRecorder.component.$server.handleRecordingError(err.message);" +
        "    }" +
        "  }," +
        "  stop: function() {" +
        "    if (window.VaadinSpeechRecorder.mediaRecorder && window.VaadinSpeechRecorder.mediaRecorder.state === 'recording') {" +
        "      window.VaadinSpeechRecorder.mediaRecorder.stop();" +
        "    }" +
        "     if (window.VaadinSpeechRecorder.stream) {" +
        "        window.VaadinSpeechRecorder.stream.getTracks().forEach(track => track.stop()); " + // Ensure mic is released if stopped early
        "     }" +
        "  }" +
        "};" +
        // Pass the reference to this Java component ($0) to the JS init function
        "window.VaadinSpeechRecorder.init($0);",
      getElement() // Pass the element reference ($0 in JS)
    );
  }

  private void startRecording() {
    // Call the JavaScript 'start' function
    UI.getCurrent().getPage().executeJs("window.VaadinSpeechRecorder.start();");
  }

  private void stopRecording() {
    // Call the JavaScript 'stop' function
    UI.getCurrent().getPage().executeJs("window.VaadinSpeechRecorder.stop();");
  }

  // This method is callable from the JavaScript code
  @ClientCallable
  public void handleAudioData(String base64AudioData) {
    log.info("Received Base64 audio data from client (length: {})", base64AudioData.length());
    try {
      byte[] audioBytes = Base64.getDecoder().decode(base64AudioData);
      String transcription = transcriptionService.transcribeAudio(audioBytes);

      // Update UI from background thread requires access()
      getUI().ifPresent(ui -> ui.access(() -> {
        transcriptionArea.setValue(transcription);
        statusLabel.setText("Status: Idle");
        progressBar.setVisible(false);
        startRecordButton.setEnabled(true); // Re-enable start button
        stopRecordButton.setEnabled(false);
        if (transcription.startsWith("Error:")) {
          Notification.show(transcription, 3000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
          Notification.show("Transcription complete!", 2000, Notification.Position.MIDDLE)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        }
      }));
    } catch (IllegalArgumentException e) {
      log.error("Error decoding Base64 audio data", e);
      getUI().ifPresent(ui -> ui.access(() -> {
        handleProcessingError("Error decoding audio data.");
      }));
    } catch (Exception e) {
      log.error("Error processing audio data", e);
      getUI().ifPresent(ui -> ui.access(() -> {
        handleProcessingError("Error processing audio: " + e.getMessage());
      }));
    }
  }

  // This method is callable from the JavaScript code for errors
  @ClientCallable
  public void handleRecordingError(String errorMessage) {
    log.error("JavaScript recording error: {}", errorMessage);
    getUI().ifPresent(ui -> ui.access(() -> {
      Notification.show("Recording Error: " + errorMessage, 5000, Notification.Position.MIDDLE)
        .addThemeVariants(NotificationVariant.LUMO_ERROR);
      resetUIState();
    }));
  }

  // Helper method to handle generic processing errors
  private void handleProcessingError(String message) {
    Notification.show(message, 5000, Notification.Position.MIDDLE)
      .addThemeVariants(NotificationVariant.LUMO_ERROR);
    resetUIState();
  }

  // Helper to reset UI if something goes wrong
  private void resetUIState() {
    statusLabel.setText("Status: Error");
    progressBar.setVisible(false);
    startRecordButton.setEnabled(true);
    stopRecordButton.setEnabled(false);
    // Try to ensure microphone is released via JS, just in case
    UI.getCurrent().getPage().executeJs("if (window.VaadinSpeechRecorder && window.VaadinSpeechRecorder.stream) { window.VaadinSpeechRecorder.stream.getTracks().forEach(track => track.stop()); }");
  }
}
