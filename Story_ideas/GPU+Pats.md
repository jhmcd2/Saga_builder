
Part 1: Consolidated Rig Hardware Manifest

Because you are running two distinct setups powered by a single power unit, you must double the data links while integrating a unified power-delivery chain.

1. Data Path Infrastructure (Quantity: 2 Setups)

• 2x eGPU Docking Boards: OwlTree PCIe 4.0 x4 eGPU Docking Stations (or similar open-frame boards like the Minisforum DEG1 or Rollbacks & More base boards).
• 50cm SFF-8611 (OCuLink 4i) Male-to-Male Cables: 2x Data Interface Cables: 50cm SFF-8611 (OCuLink 4i) Male-to-Male Cables (Note: These usually come bundled inside the OwlTree docking kits).
• JMT M.2 NVMe M-Key to OCuLink 4i (SFF-8611) Female Extension Adapters: 2x Motherboard Riser Cards: JMT M.2 NVMe M-Key to OCuLink 4i (SFF-8611) Female Extension Adapters. These sit natively inside your computer's storage slots to pass the signal externally.

2. Power & Delivery Systems (Shared Across Rig)

• 1x Desktop Power Supply (PSU): A high-quality 750W or 850W Gold-Rated ATX PSU.
	• Cable Check: Ensure it natively features at least two independent PCIe cable runs (each carrying 6+2 pin connectors) so you can dedicate one full cable branch to each K40 (each card needs one 8-pin and one 6-pin connection).
• 1x Power Bridging Cable: Dual PSU 24-Pin ATX Power Splitter Cable (often labeled as a "Dual PSU Mining Starter Cable"). This accepts the PSU's master 24-pin cable and splits it into two male 24-pin connectors to jump-start both separate OCuLink boards simultaneously upon computer boot.
• 2x Fan Power Converters: 4-Pin PWM to Molex (or SATA) peripheral power adapter cords. These pull continuous 12V juice directly from the PSU’s secondary utility lines to spin your custom K40 cooling shrouds at 100% max speed.
