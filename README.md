<p align="center">
  <img src="design/ansit-icon.svg" width="120" height="120" alt="AnsIt Logo">
</p>

<h1 align="center">AnsIt</h1>

<p align="center"><strong>A safer Android AI question-answering assistant</strong></p>

<p align="center">
  <a href="https://ko-fi.com/J1F02AL174"><img src="https://ko-fi.com/img/githubbutton_sm.svg" alt="ko-fi"></a>
</p>

<p align="center">
  <a href="https://ansit.tgwgroup.ltd">Official website</a> ·
  <a href="https://ansit.tgwgroup.ltd/#download">Download</a> ·
  <a href="#quick-start">Usage guide</a> ·
  <a href="https://github.com/AntiOblivionis/AnsIt">GitHub</a> ·
  <a href="#support-the-project">Support the project</a>
</p>

<p align="center">Android 12+ · Root + LSPosed · MIT License</p>

**Long-press the status bar to let AI understand the question and provide the answer directly on screen.**

AnsIt puts anti-detection at the center of its design. It minimizes the extra signals that target apps can observe across triggering, screen recognition, and answer display, while preserving the original screen image for ordinary capture.

## Anti-detection throughout the answering process

- **Triggering remains in the system interface.** Long presses are recognized only in SystemUI. No AnsIt gesture handler is injected into the target app, and no additional full-screen touch window is added.
- **Screen capture avoids the system screenshot interface.** The current screen is captured through Root. Normal use does not open the screenshot tool or save screenshots to the photo gallery.
- **Answers are displayed independently while capture preserves the original image.** The answer layer is excluded from standard screen capture, preventing the answer from being recorded and avoiding an opaque block that could cover the original screen. In verified environments, the original image retained by ordinary capture is consistent before and after answers are shown or hidden.

Real long-press, screenshot, and recording event observations, ordinary-permission capture, and original-image pixel comparison tests are available.[See dedicated verification and scope →](https://ansit.tgwgroup.ltd/evidence/)

Actual behavior depends on the system, ROM, and detection methods used by each app. Refer to the verification scope and test your specific device.

## Make answering more convenient

- **Light vibration feedback; release to begin.** When the long press reaches the configured duration, AnsIt vibrates briefly. Releasing enters continuous answering mode. A single tap starts the next question with context cleared by default; a double tap starts the next question with context retained by default; another long press exits. Single-tap and double-tap behavior can be swapped in Settings.
- **Use the model of your choice.** Connect an OpenAI-compatible vision model endpoint by entering the address, API key, and model name.
- **Retain context when needed.** A single tap starts a new session for the next question. A double tap carries the current screenshot and answer into the same session for the next question. When context fills up, AnsIt shows a notification. A single tap clears context and continues, while a long press exits.
- **Search only when necessary.** Optionally connect DeepSeek web search so the model can look up public information when needed.
- **Adapt to your screen.** Material 3 Monet colors are supported, along with controls for answer-area position, size, and transparency.

## Quick start

### 1. Prepare the device

- Android **12 or later**.
- **Root** access.
- A working **LSPosed** installation.
- A model endpoint with image input support and an API key.

Triggering uses Android's common status-bar entry points and does not depend on Xiaomi-specific classes. Different ROMs still expose different system interfaces, so a successful installation does not guarantee complete compatibility with every device.

### 2. Install and activate

1. [Download the APK](https://ansit.tgwgroup.ltd/#download) from the official website and install and open AnsIt.
2. Follow the in-app instructions and grant Root permission.
3. In **LSPosed → Modules → AnsIt**, enable the module.
4. Select only **SystemUI (com.android.systemui)** as the scope.
5. Return to AnsIt and use the setup guide to **restart SystemUI**, then wait for the status bar to recover.

### 3. Connect a model

In **Model Configuration**, enter:

| Setting | Value |
| --- | --- |
| API endpoint | A service-provider OpenAI-compatible address. Base URLs, `/v1` URLs, and complete `/chat/completions` URLs are supported. |
| API key | The API key issued by the service provider. |
| Model name | The model ID provided by the service provider; it must support image input. |

Tap **“Check and Save”**. Once the check passes, you can begin.

The check sends a test image and makes a real API request. Any charges are determined by the rules of the provider you use.

### 4. Begin answering

1. Tap **“Start”** in AnsIt and wait until the service is ready.
2. Open the screen you want to analyze and **long-press near the battery icon in the status bar**.
3. When you feel a vibration, release to enter continuous answering mode and wait for the answer to appear.
4. **Single-tap the battery**: next question without retaining context. **Double-tap the battery**: next question while retaining context.
5. Long-press the same area again to exit continuous answering. If context is full, a single tap clears context and continues, and a long press exits before you can start again.

If the battery layout cannot be detected, **long-press the far right of the status bar**. If the device has no vibration motor, hold for the configured long-press duration and then release.

When finished, tap **“Stop”** in the app. Closing the settings page does not stop a running service.

### Optional: Enable web search

In **DeepSeek Web Search**, enter your DeepSeek platform API key, enable **“Allow AI to search when needed”**, and tap **“Check and Save”**.

When search is enabled, the main model must support both image input and tool calls. After the check passes as described in the interface, the model will search when needed or answer directly when appropriate. Disabling search preserves the key for later use.

## FAQ

**Holding the battery has no effect?**

Confirm that the service is running, the LSPosed module is enabled, and the scope is correct, then restart SystemUI once. If the battery layout is unusual, try long-pressing the far right of the status bar. Triggering is disabled on the lock screen and when the service is stopped or ready state cannot be confirmed. If the status bar is hidden in fullscreen mode, bring it out first.

**Single-tap or double-tap has no effect?**

When the current question is still being processed, single-tap and double-tap actions are ignored. Try again after the answer appears. When context is full, double-tap only shows a notification. A single tap clears context and continues, while a long press exits and allows restarting the session.

**What should I do after updating by installing an APK with the same package name?**

Open AnsIt and let the app check the Root service and module state, then restart SystemUI once so the new module is loaded. Overwriting the APK does not replace code already loaded into SystemUI.

**Where are screenshots sent?**

Screenshots are sent directly to the model endpoint you configured. The AnsIt website does not relay them. When web search is enabled, search keywords are sent to DeepSeek. API keys are stored in the app's private directory. Use trusted endpoints and a trusted device environment.

**Are AI answers always correct?**

No. Screenshot quality, question conditions, and model capability all affect the answer. Verify important results independently.

More details are available in the [User Guide](https://ansit.tgwgroup.ltd/guide/) and [Privacy Information](https://ansit.tgwgroup.ltd/privacy/).

## Author and feedback

Developed by **[AntiOblivionis](https://github.com/AntiOblivionis)**.

- Personal homepage: [id.tgwgroup.ltd](https://id.tgwgroup.ltd)
- Project website: [ansit.tgwgroup.ltd](https://ansit.tgwgroup.ltd)
- Feedback: [GitHub Issues](https://github.com/AntiOblivionis/AnsIt/issues)
- Email: [antioblivionis@gmail.com](mailto:antioblivionis@gmail.com)

Device compatibility reports, improvement suggestions, and pull requests are welcome. When reporting a problem, include the app version, Android/ROM version, and reproduction steps. Remove personal information and API keys from screenshots and logs.

## Support the project

If AnsIt is useful to you, voluntary support is welcome through [Ko-fi](https://ko-fi.com/J1F02AL174). Thank you!

## Open-source license

This project uses the [MIT License](LICENSE), which permits use, modification, and distribution while requiring the copyright and license notice to be preserved. Third-party components follow their respective licenses.

Copyright © 2026 AntiOblivionis.
