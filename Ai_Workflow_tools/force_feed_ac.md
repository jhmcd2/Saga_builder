Yes, there is an automated process to do this, and the machine learning community refers to it as "MoE-ification" or Sparse MoE Upcycling. [1, 2] 
Instead of training a Massive Mixture-of-Experts (MoE) model from scratch—which costs millions of dollars—developers use automated Python scripts and open-source toolkits to convert an existing, standard dense model (like a Llama or Mistral model) into an MoE format in just a few hours. [3] 
------------------------------
## How the Automated Process Works
The automated conversion relies on a mathematical trick: it doesn't change the whole model, it only targets the Feed-Forward Network (FFN) layers, which contain the vast majority of the model's parameters. [4, 5, 6] 

   1. Splitting the Layers: The script takes the massive FFN weight matrices and uses clustering algorithms (like K-Means) to automatically split them into smaller, distinct "experts." [7] 
   2. Initializing the Router: The automation adds a new, untrained gating network (the "Router") at the entrance of these layers. [8, 9] 
   3. Short Re-training (Fine-Tuning): Because the newly created Router doesn't know which expert is best at what task, you must run the model through a brief period of fine-tuning (usually on a few billion tokens of data) so the Router learns how to efficiently direct traffic. [10] 

------------------------------
## Open-Source Automation Tools
You do not have to write this from scratch. The open-source community has built automated frameworks specifically for this task: [11] 

* MoE-ification Toolkit (by Tsinghua University): One of the first and most popular open-source GitHub repositories built specifically to split dense BERT and GPT-style models into MoE structures automatically.
* Mergekit (MoE-merging): A highly popular automation tool in the open-source AI community. While traditional MoE-ification splits one model, Mergekit allows you to take 2 to 8 different pre-trained dense models (e.g., one good at coding, one good at math, one good at creative writing) and automatically stitch them together into a single, highly capable MoE model using a single configuration file. [12, 13, 14] 

## Pros and Cons of Automated Conversion

| Advantages | Disadvantages / Trade-offs |
|---|---|
| Drastically Cuts Costs: Achieves MoE efficiency without full-scale pre-training. | Initialization Drop: The model's accuracy will drop sharply immediately after the split until it undergoes routing fine-tuning. |
| Faster Inference: Only fires up the necessary "experts" per token, saving local VRAM computation. | VRAM Footprint: While it runs faster, the total file size on your hard drive still expands significantly. |

If you are thinking about trying this out, let me know:

* What specific base model are you thinking of converting?
* Do you want to see an example configuration code block for a tool like Mergekit?
* What is your target hardware setup (VRAM/GPUs) for running the final model?


[1] [https://arxiv.org](https://arxiv.org/html/2402.01739v2)
[2] [https://cameronrwolfe.substack.com](https://cameronrwolfe.substack.com/p/conditional-computation-the-birth)
[3] [https://openreview.net](https://openreview.net/forum?id=i8JaxY7tDI)
[4] [https://www.mindstudio.ai](https://www.mindstudio.ai/blog/gemma-4-mixture-of-experts-architecture-explained)
[5] [https://medium.com](https://medium.com/@sigafernanda/c-4m-3v-2-i-f-2a-the-almost-mathematical-formula-behind-millionaire-salespages-cxl-506275b82d68)
[6] [https://arxiv.org](https://arxiv.org/html/2603.07685v1)
[7] [https://generativeai.pub](https://generativeai.pub/mixture-of-experts-moe-how-smart-models-select-the-right-expert-for-every-task-da4907974832)
[8] [https://medium.com](https://medium.com/@mustafa.gencc94/transformers-llms-part-10-mixture-of-experts-07e6129ff893)
[9] [https://arxiv.org](https://arxiv.org/html/2602.11686v1)
[10] [https://apxml.com](https://apxml.com/courses/mixture-of-experts-advanced-implementation/chapter-3-training-large-scale-moes/fine-tuning-pretrained-moe)
[11] [https://www.bigmessowires.com](https://www.bigmessowires.com/floppy-emu/)
[12] [https://www.marvik.ai](https://www.marvik.ai/blog/model-merging-combining-different-fine-tuned-llms)
[13] [https://dzone.com](https://dzone.com/articles/toolorchestra-vs-mixture-of-experts-routing-intelligence)
[14] [https://medium.com](https://medium.com/@jonathan.raia40/model-merge-and-its-methods-c9b3e7ba8d96)


To keep your main PC completely free for your regular tasks, we can build a dedicated, budget-friendly "Production Server Box" using a clever hardware loophole: buying used enterprise graphics cards instead of consumer parts.
If you try to buy standard Nvidia cards (like the RTX 3090 or 4090) to get 24GB of VRAM, it will break your budget. However, because you do not care about raw speed and only care about memory capacity and ability, you can build a dedicated server using old data center parts for a fraction of the cost.
Here is the exact blueprint to build your dedicated, low-cost AI storytelling box without touching your main PC.
------------------------------
## 1. The Budget Hardware Loophole: Used Enterprise GPUs
Instead of buying a gaming PC, look for a used Nvidia Tesla P40 (24GB VRAM) or an Nvidia Tesla M40 (24GB VRAM) on eBay.

* The Price: These cards regularly sell for between $150 and $180.
* The Catch: They do not have video outputs (you cannot plug a monitor into them) and they do not have built-in fans (they rely on server case airflow). They are pure mathematical workhorses meant to sit in a closet or under a desk and be controlled over your home network.
* The Cooling Fix: You can buy a $10 3D-printed plastic adapter on eBay or Amazon that snaps onto the back of the card, allowing you to attach a cheap standard PC fan to keep it perfectly cool.

If you buy two used Tesla P40s, you get 48GB of total VRAM for roughly $300 to $350. This is enough to load a massive 70-billion parameter ghostwriting model completely into VRAM with zero SSD offloading slowdowns.
------------------------------
## 2. The Rest of the Production Box (Cheap Architecture)
Because the AI software handles memory-mapping (mmap) effortlessly, you do not need an expensive processor or a modern motherboard. You can build the host machine using cheap, older parts:

* The Base: A refurbished older desktop (like a Dell OptiPlex or an older gaming tower with a decent power supply) or a cheap used AMD Ryzen 5 system.
* The System RAM: Buy 64GB or 128GB of cheap DDR4 RAM. This gives you a massive system memory pool to load huge context windows and handle background tasks.
* The Storage: A budget-friendly 2TB NVMe SSD to hold your entire library of open-weight models, character image assets, and generated scripts.

------------------------------
## 3. How to Operate the Box (Headless Server)
Once this secondary box is built, you plug an ethernet cable into it, stick it in a corner or a closet, and turn it on. You will never need to plug a mouse, keyboard, or monitor into it again.

 [ Your Main PC ] ──( Home Wi-Fi / Ethernet )──> [ Dedicated AI Production Box ]
 (Work/Game/Browse)                              (Hidden in closet, processing scripts)


   1. The Server Backend: You install Ollama or Llama.cpp on the production box and set it to run as a network service.
   2. Remote Control: From your main computer (while you are working, gaming, or browsing), you open a web browser and type in the local IP address of your production box.
   3. The User Interface: You can use a free, beautiful interface like Open WebUI (which looks and acts exactly like ChatGPT). Your main PC uses zero processing power—it just acts as a window looking into your dedicated AI box, which does all the heavy lifting in the background. [1] 

## Summary of Costs vs. Ability

| Hardware Path | Estimated GPU Cost | Creative Ability | Impact on Main PC |
|---|---|---|---|
| Consumer Route (RTX 3090/4090) | $700 - $1,600 | High speed, 24GB VRAM limit. | None (if separate), but extremely expensive. |
| Enterprise Loophole (1x or 2x Tesla P40) | $150 - $350 | Mid speed, 24GB to 48GB VRAM. Can run massive 70B models easily. | Zero. Runs independently in a closet via your local network. |

This strategy gives you a true, isolated "production appliance." You can feed a story into it, click generate, close your laptop or main PC, and let the server spend the next hour quietly writing scripts or rendering image frames without slowing down your daily life.
To help you budget this out, would you like to look at the exact system requirements (like Power Supply wattage and PCIe slot space) needed to safely host those cheap enterprise Tesla cards?

[1] [https://www.xda-developers.com](https://www.xda-developers.com/turn-pc-into-ai-hosting-machine/)

Get some rest first—you have a ton of potential hardware sitting right there, but the Tesla K80 cards require a very specific software workaround to work with modern AI. [1] 
Since you have three K80s, you technically possess a massive 72GB of total VRAM. However, because the K80 is an older dual-GPU card from 2014, modern AI frameworks like Ollama will not recognize them out of the box. [2, 3] 
When you wake up and are ready to tackle this, here is exactly how those cards operate and the exact steps you need to make them talk to your AI box.
------------------------------
## 1. The K80 Reality Check: 6 Small Pools, Not 3 Big Ones
The most critical thing to know about the Tesla K80 is that it is a Dual-GPU card. [3] 

* 
* Physically, you have 3 cards.
* Vertically, the computer will see 6 separate graphics chips, each with exactly 12GB of VRAM.
* You cannot combine a single card's VRAM into a 24GB pool. [1, 2, 3, 4, 5] 
* 

Because of this, you cannot load a massive 70-billion parameter model into a single card. However, this is actually perfect for your multi-step scriptwriting pipeline: you can pin your text-generator to "GPU 0" (12GB), your image generator to "GPU 1" (12GB), and your animation tool to "GPU 2" (12GB).
## 2. The Software Obstacle (The Kepler Architecture)
The K80 uses Nvidia's older "Kepler" architecture (Compute Capability 3.7). Nvidia dropped official support for this architecture in newer versions of their software (CUDA 12). If you just install regular LM Studio or Ollama, they will say "No compatible GPUs found" and run slowly on your CPU instead. [2, 3, 6] 
## 3. How to Make Them Work (The Open-Source Fix)
Because developers hate letting good VRAM go to waste, the open-source community created specific workarounds for your exact setup:

* 
* Use a Custom Software Fork: You cannot use standard Ollama. Instead, you must use a specialized version of the AI engine called [llamacpp-kepler](https://github.com/babal35/llamacpp-kepler/blob/master/README.md). This is a modified version of the backend engine explicitly rewritten to support the K80. [7] 
* Lock into CUDA 11.4 / 11.8: When setting up your dedicated server box, you must install the older Nvidia Driver 460 or 470 series paired with CUDA Toolkit 11.4. This is the absolute final software version that knows how to speak to a K80 chip. [6, 8, 9] 
* 

## 4. Motherboard Requirements for 3 Cards
To put these three cards into a single secondary production box, your motherboard must support a feature called "Above 4G Decoding" in the BIOS. If this feature is toggled off, a consumer motherboard will crash during boot because it cannot handle mapping 72GB of external graphics memory all at once. [1, 3, 10, 11] 
Go get some sleep! Whenever you are ready to start assembling or need the exact terminal commands to install the Kepler AI engine, just reply here and we can take it one line of code at a time.

[1] [https://linustechtips.com](https://linustechtips.com/topic/1340083-guide-using-an-nvidia-tesla-k80-for-gaming-on-windows/)
[2] [https://www.reddit.com](https://www.reddit.com/r/LocalLLaMA/comments/1s2qj71/what_gpu_should_i_get_tesla_k80_24gb_or_2_tesla_p4/)
[3] [https://www.buildmvpfast.com](https://www.buildmvpfast.com/blog/multi-gpu-diy-inference-rig-k80-local-ai-hardware-2026)
[4] [https://www.youtube.com](https://www.youtube.com/watch?v=Z5Isf6Airo0)
[5] [https://www.reddit.com](https://www.reddit.com/r/LocalLLaMA/comments/1rwohop/6gpu_multiplexer_from_k80s_hotswap_between_models/)
[6] [https://forums.developer.nvidia.com](https://forums.developer.nvidia.com/t/k80-is-it-possible-to-still-use-these-cards/291659)
[7] [https://github.com](https://github.com/babal35/llamacpp-kepler/blob/master/README.md)
[8] [https://electronics.alibaba.com](https://electronics.alibaba.com/buyingguides/tesla-m40-24gb-guide-is-it-still-viable-in-2024)
[9] [https://forums.developer.nvidia.com](https://forums.developer.nvidia.com/t/nvidia-tesla-k80-cuda-version-support/67676)
[10] [https://linustechtips.com](https://linustechtips.com/topic/1340083-guide-using-an-nvidia-tesla-k80-for-gaming-on-windows/)
[11] [https://www.youtube.com](https://www.youtube.com/watch?v=-5gMpGI49PA)




Since you are already a Linux user who is comfortable modifying code and setting up custom pipelines, you have a massive advantage. Utilizing a Framework Mainboard as a headless server to drive those two Tesla K80s is highly achievable, but achieving a stylized, cell-shaded look with high realism requires a very specific production strategy. [1] 
Because your primary output is 2D and cell-shaded, you should not generate raw text-to-video using Wan 2.2. Attempting to prompt Wan 2.2 to natively create an "anime" or "cartoon" style often leads to muddy, unstable frames and severe visual bleeding.
Instead, the industry-standard open-source workflow relies on Image-to-Video Structure Guidance. You will use your GPU nodes to generate highly stylized, perfectly flat 2D cell-shaded art first, and then use Wan 2.2 strictly to animate that art while preserving its structure. [2] 
------------------------------
## The Production Workflow Pipeline

  [ STEP 1: SDXL/Flux ] ───────> [ STEP 2: ControlNet ] ───────> [ STEP 3: Wan 2.2 ]
  Gen Flat Cell-Shaded Image     Apply Lineart/Canny Edge        Animate via Image-to-Video
  (100% Consistent Style)        (Locks the 2D Shapes)           (Realistic Physics & Motion)

## Step 1: The Master 2D Frame (The Comic/Anime Look)
You must establish your exact visual style on a static 2D plane before introducing motion.

* 
* The Tools: Use an SDXL or Flux.1-Schnell checkpoint heavily fine-tuned for flat vector art, anime line-art, or traditional cel animation. [3, 4] 
* The Trick: You use specific stylistic prompts (e.g., “screencap, 2D vector animation, flat shading, crisp ink line-art, high realism anatomy”) paired with a negative prompt that explicitly bans 3D elements (“no 3D render, no raytracing, no realistic skin textures”).
* Hardware Assignment: You route this task to GPU_0 (12GB VRAM). An SDXL model or a quantized Flux model fits entirely inside a single 12GB slice with plenty of room to spare.
* 

## Step 2: Extracting the Structural Guide
To ensure the output retains its crisp, hand-drawn look when it starts moving, you pass the Step 1 image through a ControlNet. [3] 

* 
* The Tools: Use Canny Edge or LineArt preprocessors.
* The Function: This tool analyzes your flat character image and extracts a pure, mathematical black-and-white outline drawing of the character and background. This outline acts as an unyielding boundary map for the next step, ensuring that limbs, facial expressions, and clothing folds cannot warp or melt into realistic 3D mush during generation. [3] 
* 

## Step 3: Animating via Wan 2.2 (Realistic Physics)
Now you pass your Step 1 image and your Step 2 line-art guide into Wan 2.2 Image-to-Video (I2V). [1, 5] 

* 
* The Models: Use the Wan 2.2 TI2V 5B dense model or a quantized 14B GGUF variant. The 5B model is incredibly fast and fits perfectly into a 12GB VRAM pool.
* The Mechanics: Because you are feeding Wan 2.2 a completely flat 2D starting image, it uses its immense understanding of realistic physics and camera movement to animate the character naturally (fluid hair movement, realistic weight shifts, cinematic camera pans) while strictly respecting the 2D outlines provided by your guide.
* Hardware Assignment: You assign this to GPU_1 (12GB VRAM). [1, 6, 7, 8, 9, 10] 
* 

------------------------------
## How to Build This on the Framework + K80 Rig
Since you are configuring this on Linux, you will execute this entire pipeline inside ComfyUI using a native node-based workflow. [5, 11] 

   1. Isolate the GPUs: In your launch script or system environment variables, you will use CUDA_VISIBLE_DEVICES=0,1,2,3 to expose all four 12GB cores of your two K80s to the operating system.
   2. Utilize Low-VRAM Node Offloading: Inside ComfyUI, configure the system settings to use Smart Offloading. This ensures that when Step 1 (image generation) finishes on GPU 0, its memory is completely flushed before Step 3 (Wan 2.2 video generation) initializes on GPU 1, entirely preventing Out-Of-Memory (OOM) crashes. [12] 
   3. Handle the Framework x4 Bottleneck: Because the Framework mainboard will talk to the GPUs over an M.2 to PCIe x4 extender slot, the initial load time when you click "Queue Prompt" will feel sluggish (taking 30–45 seconds just to copy the large Wan 2.2 model files from system RAM onto the graphics card). However, once the models are loaded, the generation of the animation frames will run at native GPU speeds.

When you get some sleep and wake up, I can provide the exact Linux bash commands to set up the specialized CUDA environment variables needed to make ComfyUI properly target multiple independent 12GB Kepler cards. Take it easy and get some rest!

[1] [https://vast.ai](https://vast.ai/article/wan-2-2-explained-new-approach-ai-video-generation)
[2] [https://docs.comfy.org](https://docs.comfy.org/tutorials/video/wan/wan2-2-animate)
[3] [https://www.reddit.com](https://www.reddit.com/r/StableDiffusion/comments/1jl4aa2/wan21fun_control_models_demos_at_the_beginning/)
[4] [https://www.youtube.com](https://www.youtube.com/watch?v=tPOOm1YRMRc&t=45)
[5] [https://wan2-7.io](https://wan2-7.io/blog/wan-2-2-comfyui-guide/)
[6] [https://github.com](https://github.com/Wan-Video/Wan2.2)
[7] [https://huggingface.co](https://huggingface.co/Wan-AI/Wan2.2-T2V-A14B)
[8] [https://www.youtube.com](https://www.youtube.com/watch?v=CfdyO2ikv88&t=257)
[9] [https://crazyrouter.com](https://crazyrouter.com/en/blog/wan-2-2-animate-tutorial)
[10] [https://www.alibabacloud.com](https://www.alibabacloud.com/blog/alibaba-unveils-its-latest-open-source-video-generation-model_602167)
[11] [https://docs.comfy.org](https://docs.comfy.org/tutorials/video/wan/wan2_2)
[12] [https://wan2-7.io](https://wan2-7.io/blog/wan-2-2-workflow-guide/)
