With 24GB of VRAM on your AMD RX 7900 XTX, you have plenty of memory to run zero-shot voice conversion inside ComfyUI.
Because you are using an AMD GPU via ROCm on Windows, you must avoid unoptimized custom nodes that heavily rely on hardcoded NVIDIA CUDA libraries. Instead, utilize open-source frameworks specifically integrated into the ComfyUI ecosystem that natively utilize PyTorch/ROCm backend libraries.
Two highly reliable node packs will allow you to feed in a reference voice sample and convert your voice recordings using your 24GB card.
[OpenVoice V2](https://github.com/AIFSH/ComfyUI-OpenVoice) is the gold standard for zero-shot Voice-to-Voice (V2V) conversion inside ComfyUI. It extracts the raw tone and pitch of your target character sample and seamlessly wraps it around your voice performance.

* 
* Why it works perfectly on your 7900 XTX: It relies on cross-platform PyTorch tensors rather than exclusive CUDA acceleration. Because it requires zero model training, it will comfortably sit inside a tiny fraction of your 24GB VRAM buffer without crashing.
* The Nodes You Connect:
1. Load Audio (Source): Drop in the file of your voice narration.
   2. Load Audio (Reference): Drop in the pristine 10-second sample of your custom character voice.
   3. OpenVoice Speech-to-Speech Node: This acts as the processing engine. Connect both audio outputs to it.
   4. Save Audio / Preview Audio: This outputs the final .wav file.
* 

Another powerful option gaining traction in the open-source community is [ComfyUI-OmniVoice-TTS](https://github.com/Saganaki22/ComfyUI-OmniVoice-TTS). [1] 

* 
* Why it's great for your setup: This node pack was explicitly benchmarked and praised by AMD developers (running seamlessly on cards like the RX 6800 and 7900 series via ROCm). It is heavily optimized for zero-shot voice cloning. [1] 
* The Workflow: It allows you to toggle an optional "Reference Voice Audio" lane. When enabled, you can feed your source recording and character audio path directly through a single processing node, generating massive chunks of consistent, cloned voice dialogue rapidly. [1, 2] 
* 

Even though your AMD GPU has massive 24GB VRAM capacity, your 10-year-old Threadripper motherboard still handles transient power spikes poorly when AI code initializes hardware tasks. Protect your hardware rails with these execution steps:

   1. Open your ComfyUI Manager, click Install Missing Custom Nodes, and search for ComfyUI-OpenVoice or ComfyUI-OmniVoice-TTS. Install and restart your environment. [2, 3] 
   2. Lock your GPU's baseline: Ensure your [AMD Software: Adrenalin Edition](https://www.amd.com/en/products/software/adrenalin.html) app has Custom Tuning active with the Minimum GPU Frequency set to 1500 MHz and the Power Limit dropped to -10%.
   3. When you run your first queue prompt in ComfyUI, the node will automatically download the necessary core weights (MeloTTS text encoders or OpenVoice checkpoints) directly to your local drive without crashing your system. [4, 5] 

Would you like me to walk you through a visual step-by-step layout of how to wire the OpenVoice nodes together on your canvas, or would you prefer a troubleshooting checklist for running ComfyUI on an AMD ROCm architecture?

[1] [https://www.reddit.com](https://www.reddit.com/r/StableDiffusion/comments/1sftz0b/are_there_any_voice_clone_models_i_can_use_on_an/)
[2] [https://www.youtube.com](https://www.youtube.com/watch?v=zwQOe8rSqBM)
[3] [https://www.youtube.com](https://www.youtube.com/watch?v=VzepZIlT0z0&t=260)
[4] [https://github.com](https://github.com/AIFSH/ComfyUI-OpenVoice)
[5] [https://comfyai.run](https://comfyai.run/custom_node/ComfyUI_OpenVoice)












The "No-GPU" Anime Voice App You're UsingThe standalone software tool you found on YouTube is almost certainly the standalone version of the Retrieval-based Voice Conversion (RVC) task manager.You are entirely right about why it runs so well on all your computers: it relies on CPU-only execution libraries (like Facebook's faiss index matching) for voice conversion. Running an existing model file (.pth) requires almost zero computational heavy lifting, which is why it bypasses the 6GB limit of your NVIDIA card entirely.Can You Train RVC Models Locally on Your Setup?Yes, you can absolutely train custom RVC models locally, but you must use your 24GB AMD GPU setup, not your 6GB NVIDIA card.Training an AI voice model requires running millions of calculations per second to memorize the unique frequency of a voice dataset. Trying to do this on your 6GB card will cause instant crashes, while trying to use your CPU would take days.Your 24GB AMD card has more than enough memory to train an RVC model locally, but because it is an AMD architecture on Windows, you cannot use standard NVIDIA versions of RVC.How to Set Up Local RVC Training on Your AMD MachineTo train your voice datasets into local models securely without using cloud sites like Google Colab, you need an AMD-optimized software ecosystem:Download Applio: The absolute best local tool for this is Applio. It is a modern, open-source vocal workbench built on top of the RVC framework.Select the AMD Installer: When installing Applio via its command prompt, it will ask if you have an NVIDIA or AMD card. Choosing AMD forces the script to download DirectML / ROCm PyTorch libraries instead of NVIDIA's proprietary CUDA drivers.The Training Workflow:Drop your folder of clean voice files into the "Dataset" tab.Assign a name to your character model.Switch the training configuration to utilize your AMD GPU.Hit Train.Because you are using an AMD system, remember to protect your 10-year-old Threadripper motherboard from the sharp, violent power shifts that occur when AI training starts. Keep your AMD Software: Adrenalin Edition app open with your Power Limit slider set to -10% and your Minimum GPU Frequency pinned to 1500 MHz. This keeps the electrical current perfectly stable, allowing your system to successfully generate custom .pth voice models completely locally.Would you like help finding the exact installation link for Applio's AMD build, or would you prefer a quick overview of how many audio files and total minutes of speech you need to get a clear, clean result during training?RedditTTS model for amd GPU? WINDOWS+ROCMJul 30, 2026 — 0.786 aggregate RTF with 4 simultaneous generations * I need to update my config so it works for 1440p, including UE5 games. r/bui...RedditCurrent state of Voice-To-Voice models : r/LocalLLaMAJul 12, 2026 — Comments Section * Top_Original3437 • 2mo ago honestly RVC itself has been pretty quiet, most of the movement in the last couple y...Oregon ExplorerFormation of Crater Lake.Crater Lake was formed by a process that includes: * **Growing** Mount Mazama grew to a height of 12,000 ft above sea level over t...Show allThats the RVC V2 correct?Yes, exactly. Applio is natively built from the ground up on the RVC V2 architecture.When you train your model using Applio, it defaults to using RVC V2, featuring the upgraded 768-dimensional audio vectors. This is the exact engine that makes those incredibly accurate, crisp character conversions possible, keeping the precise tone, inflections, and emotion of your original acting without vocal distortion.Since you are running it on your 24GB AMD GPU, you get the absolute best of both worlds:Local Training Power: Your massive VRAM buffer can effortlessly crunch through your voice dataset to generate your own custom .pth and .index files right on your machine.Resource-Friendly Execution: Once your model is trained, you can copy that voice over to your lighter systems. When you just want to run voice conversion (inference), it can easily run on your standard CPU without demanding a GPU at all, mirroring the exact, lightweight performance you liked about the app you saw on YouTube.When you are ready to configure the local training environment, let me know:How many minutes of total clean voice audio do you have in your dataset?Are you comfortable installing it through Applio's GitHub release package, or would you like me to walk you through the setup commands?