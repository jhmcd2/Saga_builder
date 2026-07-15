
Get responses tailored to you

Log in to get answers based on saved chats, plus create images and upload files.


Log in

Today 9:07 AM
So someone created this way to run an extremely large AI model on a laptop.  They use expert nodes (and stole the idea for my Thesis!!!!) to cut down on the amount of informatin being pulled by the model at one time. I don't know the video I saw it on, so i was wondering if you know anything abou it. 

It was AI explained!  So is a claude style frontier model able to run with this model?  

What is the largest publically availible model, and what would it take to run as a MoE model.

So what about VRAM, and let's look at a standard or at least a custom PC, although God knows where you can get that much RAM without selling a child or a vital organ.  What if you used VRAM for the storage, since hard drives are relativly cheaper and you can get them with as much as 10TB of space.  SSD's and HDDs (SATA and SAS).

Okay, for large projects, I have a desktop with a 24GB GPU which i sue for prototyping, but for larger projects that delay may not be as problematic.  Lets say I wanted to write  a 500 page novel. Let's assume for a moment I have 1000 pages of references. I would probably use a GPU for most of the proncessing, but if it could offload to SSD, that would be helpful, how long would this potentially take versus I don't know using CLaude's API. 

This is actually a great use case because novel writing is much more tolerant of latency than chat.

If you're waiting 500 ms between chat tokens, it feels sluggish. But if you're asking for "Write Chapter 12" and it takes 45 seconds instead of 10, that's often acceptable.

Here's how I'd think about it.

Your hardware
Let's assume something like:

RTX 4090 / RTX 3090 / RTX 5090-class GPU with 24 GB VRAM

128–256 GB DDR5 RAM

4–8 TB Gen5 NVMe SSD

Modern Ryzen or Threadripper CPU

That's already a very capable local AI workstation.

What a novel actually requires
A 500-page novel is roughly:

125,000–180,000 words

Maybe 180,000–250,000 tokens

The model doesn't generate all of that in one context. Instead, you'd typically use a pipeline:

World bible

Character database

Timeline

Chapter outline

Scene outline

Draft each scene

Revise

Consistency pass

Each step only needs part of the overall information.

That's actually much easier than trying to hold an entire novel in context.

The reference material
Your 1,000 pages of references might only amount to around 500,000–700,000 tokens, depending on formatting.

Rather than loading all of that into the model's context, you'd almost certainly use retrieval-augmented generation (RAG):

Store all references in a vector database.

Retrieve only the most relevant passages for the current scene.

Feed those excerpts into the model.

That dramatically reduces memory and compute requirements.

SSD offloading
Suppose your model has:

600 GB of quantized weights

24 GB VRAM

128 GB RAM

A scheduler could keep:

frequently used experts in VRAM,

others in RAM,

rarely used experts on SSD.

If you're generating a whole chapter, the router's expert usage is likely to be relatively stable, making prefetching more effective than in a back-and-forth chat.

Speed
This is where the comparison gets interesting.

A local 70B model on a 24 GB GPU with quantization might produce somewhere around 5–20 tokens per second, depending on the exact model, quantization, and how much offloading is happening.

If you introduce significant SSD streaming, throughput could drop substantially—perhaps to 1–5 tokens per second in a less optimized setup. With smarter caching and prefetching, it could be higher, but that's an active area of research rather than a solved problem.

For a 4,000-token chapter:

At 20 tokens/sec: about 3–4 minutes.

At 5 tokens/sec: about 13 minutes.

At 2 tokens/sec: roughly 30–35 minutes.

Those are rough estimates rather than guarantees, but they illustrate the trade-off.

Claude's API
Using a frontier model through an API has different strengths:

Advantages

Responses are often much faster because the provider runs optimized GPU clusters.

The model quality is currently among the best available.

No local hardware management or weight downloads.

Trade-offs

Ongoing usage costs.

You depend on an internet connection and the service's availability.

You don't have the same freedom to modify the inference pipeline or experiment with the model internals.

The more interesting comparison
For writing a novel, I actually don't think the biggest difference is raw generation speed.

It's how much orchestration you build around the model.

A sophisticated local system might include:

A vector database for your research.

Character profiles with structured metadata.

A timeline checker.

A map and location database.

Relationship tracking.

Style guides.

Revision agents that look for continuity errors.

In that setup, the language model is one component of a larger writing system.

If I were designing this in 2026
With your hardware, I'd probably aim for something like:

Research PDFs
       │
Embedding Model
       │
Vector Database
       │
Planning Agent
       │
Outline Generator
       │
Scene Writer
       │
Continuity Checker
       │
Editor
       │
Final Manuscript
The language model itself doesn't need to "remember" the entire 1,000 pages of references. Instead, each stage retrieves only what's relevant, which is generally more efficient and often produces better results than trying to keep everything in context.

Given what I know about your interests, I could also see you enjoying a more modular setup where different specialized models handle different tasks. For example, a reasoning-focused model could build the plot, another model could draft scenes, and a smaller editor model could polish prose and check continuity. That approach can make better use of a 24 GB GPU than trying to run one enormous frontier model for every step, and it's a common direction for advanced local AI workflows.


So let's design this.  I would probably use the Framework motherboard as the basis for the GPU, Assuming we are using Virtual memory instead of  VRAM to make up the differnece for this same project, assume that the RAD pipeliens and all that is cnsturcted on my desktop before being expoted with the proper context to the AI Box (so we are designing an AI Box). The model we should try to spec is a Frontier level model if possible) with MoE ability.  What would we ned to get this to work. 

This is a fascinating design exercise because you're essentially describing a personal AI inference appliance rather than a normal workstation. The architecture you are proposing is closer to a small-scale AI server: a box whose job is to host a very large model and expose it to your desktop.

A few important design choices will determine whether this is practical.

First, a clarification: virtual memory can help, but it cannot replace VRAM. You can build a system where the model weights live across GPU memory, system RAM, and SSD, but the performance hierarchy is still:

GPU VRAM / HBM     ← extremely fast
       |
System RAM         ← fast
       |
NVMe SSD           ← much slower
       |
HDD                ← archival only
The trick is not "make SSD as fast as VRAM." The trick is "make sure the model rarely needs to wait for SSD."

For your novel-writing example, that is actually a reasonable goal.

AI Box concept
The architecture:

              Desktop PC
                  |
        (planning, RAG, UI, databases)
                  |
              10/25/100Gb Ethernet
                  |
              AI Box
                  |
     --------------------------------
     |                              |
 GPU Memory                    Storage Pool
 (active experts)              (cold experts)
     |                              |
     |                         NVMe RAID
     |
 Large MoE Model
Your desktop handles:

document processing

embeddings

vector database

research organization

prompt construction

project management

The AI Box handles:

model inference

expert routing

GPU acceleration

That separation is actually a very good design.

Target model
Let's define "frontier level."

A true frontier model today is something like:

hundreds of billions to trillion+ parameters

large context window

strong reasoning ability

multimodal capability

A practical open model target would be something in the range of:

Option A: Current practical target
A 400B–700B parameter MoE model.

Example characteristics:

Total parameters:
500 billion

Active parameters:
30–50 billion

Quantized:
4-bit

Storage:
250–350 GB
This is ambitious but plausible.

Option B: Moonshot design
A trillion parameter MoE:

Total:
1 trillion+

Active:
50–100 billion

4-bit storage:
500+ GB
Now we need serious hardware.

AI Box hardware
Let's build around your Framework motherboard idea.

One caveat: Framework laptops are modular and excellent engineering, but for an AI box you would probably want a Framework-inspired modular motherboard, not a Framework laptop motherboard. GPUs need:

PCIe lanes

power delivery

cooling

multiple expansion slots

Think Threadripper Pro / workstation motherboard territory.

CPU
You need PCIe lanes more than raw CPU power.

Good choices:

AMD Threadripper Pro platform

EPYC workstation/server platforms

Target:

32–64 cores

128+ PCIe lanes

Why?

Because you want:

multiple GPUs

multiple NVMe drives

fast networking

GPU subsystem
This is the hardest part.

A realistic AI Box might have:

Consumer route
4 × RTX-class GPUs

Example:

GPU 1:
24 GB VRAM

GPU 2:
24 GB VRAM

GPU 3:
24 GB VRAM

GPU 4:
24 GB VRAM

Total:
96 GB VRAM
Now you have enough fast memory for many MoE active layers.

Professional route
Something like:

4 × 80 GB workstation/datacenter GPUs

Total:
320 GB VRAM
This starts becoming genuinely interesting.

RAM
This is where your "sell a kidney" comment comes in.

The good news:

You don't need ridiculous RAM if your architecture is smart.

I would aim for:

Minimum:

256 GB ECC RAM
Better:

512 GB ECC RAM
Extreme:

1 TB ECC RAM
The RAM becomes your expert cache.

Storage
Your instinct here is correct.

Do NOT use HDD for active inference.

Use:

8 TB NVMe Gen5 SSD
+
8 TB NVMe Gen5 SSD
+
8 TB NVMe Gen5 SSD
in a RAID configuration.

Something like:

24 TB fast storage
The HDDs are for:

archived models

datasets

backups

Networking
Your desktop should not talk to this thing over normal Ethernet.

Minimum:

10 GbE

Better:

25 GbE

Ideal:

100 GbE

The connection matters because your desktop is effectively the "brain interface."

Software stack
The interesting part is here.

You would need something like:

Operating System
        |
GPU Runtime
        |
Inference Engine
        |
MoE Router
        |
Expert Cache Manager
        |
NVMe Storage Layer
The missing piece is the Expert Cache Manager.

That is the novel part.

Something like:

             Router says:
       "Need experts 12, 45, 88"

                    |
                    v

        Check VRAM cache

        Yes ---> Run

        No

        Check RAM cache

        Yes ---> Copy to VRAM

        No

        Load from NVMe
Over time it learns:

"For fantasy novels, these experts are used constantly."

Keep them hot.

Expected performance
A realistic estimate:

A 500B MoE model:

With:

100GB+ VRAM

512GB RAM

fast NVMe

Could potentially achieve:

interactive responses

maybe 2–10 tokens/sec depending on architecture

For writing:

Totally usable.

A chapter might take:

5–30 minutes

The bottleneck becomes your workflow, not the machine.

The really interesting part
Your design is basically proposing:

"AI as a memory hierarchy."

Current AI:

Model = fixed object
Your AI Box:

Model =
    active thoughts (VRAM)
    working memory (RAM)
    long-term memory (SSD)
That is much closer to how biological systems work.

The engineering challenges are:

Predicting which experts will be needed.

Moving them fast enough.

Maintaining coherence across modules.

Making the router aware of memory cost.

Those are exactly the kinds of problems where a thesis-level contribution could exist.

If I were building a first prototype of this, I actually would not start with a trillion parameter model. I would build the memory-management layer first using a 100B–200B MoE model, because the expert-loading system is the interesting invention—not the number of parameters.




Ad

Today 11:21 AM
So I was refering to the Fromework modular motherboard, which i know you can get in a 128GB of VRAM config.  That's still cheaper than a GPU, as I can't even find my GPU which is an AMD I purchased for $700 about. However an ATX which can handle an Threadripper, that is also somewhat pausible
SO what ya got


