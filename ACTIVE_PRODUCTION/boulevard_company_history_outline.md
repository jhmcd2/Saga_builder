# Boulevard Company History – Outline

Nov 8, 2026 · @James McDonald

## 1. Purpose

`company_history.py` holds Boulevard's scripted story: events on fixed dates or in fixed eras, layered on top of the general rules in `fleet_builder.py`.

- **What it can change today:** money and contracts only (cash, loans, leases, how aircraft are paid for).
- **What it cannot change today:** demand, operations, costs per flight, the fleet, or the network.
- **Called from** `simulation.run_day` in three places:
  1. Exact-date story events (the Japanese loan, the MetroJet shutdown), before anything else that day
  2. Boulevard Holding income, on the 1st of each month
  3. `preferred_acquisition_mode` and the MetroJet lender name, used by `fleet_builder.py` when buying or leasing aircraft

## 2. Story timeline (as written in the file)

- **Before 1995:** East Coast Part 91K / Part 135 charter operation; grows enough to partner with US Airways. *Not simulated.*
- **1995:** Part 121 scheduled service launches as MetroJet, flown under the US Airways brand to avoid pilot scope clauses (sued anyway). Fleet of 737-200s leased from US Airways.
- **1998:** Most legacy types (DC-10, F100, 727-200, L-1011, MD-80, 737-900) arrive on a favorable Boeing "starter package" lease. A340 and MD-11 excluded. *Not built.*
- **Early 2001:** Growth trap and near insolvency. A Japanese investment company's loan refinances the debt.
- **30 Oct 2001:** US Airways shuts down MetroJet amid its bankruptcy. Boulevard absorbs the leased 737-200s outright.
- **2012:** Boulevard Holding formed: outside investments, including Maryland land made valuable by BRAC redevelopment, subsidize the airline (Emirates/Dubai pattern).
- **2015:** Healthy enough to pay cash for aircraft again.
- **2020 (COVID):** Reserves, Holding income, rolling furloughs and management pay cuts keep it solvent; controlled ramp-up afterward. *Not built.*

## 3. Events currently built

1. **MetroJet launch (first day simulated, 1995)** — lives in `fleet_builder.py`, not this file
   - $50M starting capital
   - 50 used 737-200s, capped at 49 by the type's fleet limit, split evenly across the active hubs (BWI, FLL)
   - Leased from "US Airways / MetroJet" at 0.8% of aircraft value per month
2. **Japanese refinancing loan (1 Feb 2001)**
   - $200M; 5% a year; 15 years; interest only (about $833k a month)
   - Pays off existing aircraft loans, oldest first; one it can't fully cover is partly paid down (monthly payment not recalculated)
   - Money left over becomes cash
   - Full $200M due in one payment on 1 Feb 2016
3. **MetroJet shutdown (30 Oct 2001)**
   - Every lease still with "US Airways / MetroJet" is closed as "Transferred"
   - Boulevard keeps the aircraft at no cost; lease payments stop
   - $0 ledger entry records it
4. **Boulevard Holding income (from 2012, 1st of each month)**
   - 2012: $0.5M a month
   - 2015: $1.5M a month
   - 2018: $3M a month
   - 2022: $5M a month
5. **How aircraft are paid for**
   - Before 2015: loans (15% down, 7%, 10 years)
   - From 2015: cash, falling back to a loan when cash is short

**Rule to know:** the two 2001 events fire only if the simulation runs on that exact date. A run that starts later skips them.

## 4. In the story but not built

- **Pre-1995 charter years** — background only; the simulation starts in 1995.
- **1998 Boeing starter package**
  - Types: DC-10, F100, 727-200, L-1011, MD-80, 737-900 (not A340, not MD-11)
  - Needs: how many of each type, which hubs, lease terms, delivery dates
- **COVID operations (2020)**
  - Rolling furloughs and management pay cuts → lower crew costs
  - Schedule contraction, then a controlled ramp-up
  - Today 2020 shows up only as a whole-year drop in the world passenger index
- **Voluntary principal paydowns on the Japanese loan** — the function exists in `fleet_builder.py` but nothing calls it, so the full $200M always falls due in 2016.

## 5. Story pieces in other files

- **Economic eras** (`seed_economic_factors.py`) — pilot/FA pay, maintenance costs, fare margin
  - 1995: budget carrier competing with Southwest/ValueJet (pilots $60/h, FAs $25/h, margin 1.05)
  - 2001: "accidentally" mainline scale (pilots $120/h, FAs $45/h, margin 1.20)
  - 2019: Emirates/Singapore-tier repositioning (pilots $180/h, FAs $70/h, margin 1.35)
  - Real U.S. jet fuel prices 1995–2020; years after 2020 reuse 2020's
- **Hubs and focus cities** (`specail_airports` data) — opening year per airport, e.g. BWI and FLL 1995, BOS 1999, IAD 2002, DTW and LAX 2003, Tokyo/Osaka 2008, Paris/London 2012; Hong Kong 2015, retired 2020
- **Aircraft eras** (aircraft data) — delivery and final-retirement dates and fleet caps per type; all trijets forced out by 2020 (`economics.py`)
- **Freighter conversions** (`cargo.py`) — 767-400 and 747-400 become freighters once 787s / 747-8s match their numbers; deadline 31 Dec 2026
- **Cargo timeline** (`cargo.py`) — operations start 2015, full capacity 2020 (COVID spike); lanes and contracts defined, no cargo flights scheduled yet
- **Demand history** (`routes.py`) — world passenger index with real figures from 2014; earlier years extended at steady growth (no 2001 or 2008 dip)

## 6. Events to add

**Template — one per event:**

- Name:
- Start date / end date:
- Where it hits: everywhere / US only / region / specific airports
- Airspace or airport closure (days, which airports):
- Demand change: how deep, and the recovery shape and length
- Cost changes (security, insurance, fuel, crew pay), with amounts and dates:
- Cash in or out (aid, grants, loans, fines):
- Fleet effects (groundings, retirements, deliveries):
- How Boulevard responds (capacity cut, mid-year re-plan, furloughs):
- Linked story events (e.g. MetroJet shutdown):

**Candidates:**

- [ ] 9/11 (11 Sep 2001) — closure, demand collapse and recovery, security and insurance costs, federal stabilization aid
- [ ] 1998 Boeing starter package (from section 4)
- [ ] 2008 fuel spike
- [ ] 2008–09 recession
- [ ] 2019 737 MAX grounding
- [ ] COVID 2020 operations (from section 4)

## 7. Placeholders and open questions

- [ ] Japanese loan date (1 Feb 2001) and amount ($200M) — placeholders; the story says only "early 2001"
- [ ] Boulevard Holding monthly income figures — placeholders
- [ ] Starting capital ($50M) — placeholder
- [ ] MetroJet shutdown date: keep 30 Oct 2001, or tie it to the 9/11 event? (Historically MetroJet ended around December 2001 — to verify.)
- [ ] Should the 1995 cold start split the fleet by demand instead of evenly across hubs? (12 FLL aircraft sat unused in 1995.)
- [ ] Boulevard market share (25%) — placeholder; per year or per market?
- [ ] Should a voluntary paydown of the Japanese loan be part of the story?
- [ ] Freighter conversion deadline for the 747-8 (2030) — marked unconfirmed in `cargo.py`





## Updated History

1. 1990 (NOT SIMULATED)
- Mid Atlantic Charter Started with a single B1900 mainly ran flights between DC and NYC as a Part 91K operator
- Mid Atlantic Charter establishes baltimore as a Hub
2. 1991 (NOT SIMULATED)
- Mid Antatlic Charter Applies for Part 135 Certificate, since the paper work is msotly the same opts to apply for Part 121 
2. 1992 (NOT SIMULATED)
- Now w/ 4 aircraft Mid Atlantic Charter takes over a number of essentual air routes in Maryland and PA.
3. 1993 (NOT SIMULATED)
- Mid Altantic Charters achieves Part 135 certificate, applies for Part 121 Certificate.
- Get's Partnership wwith US Airways
- US Airways pitches Metrojet idea
- An NYC investment Firm fund the joint venture. With US Airways as a guarentter, but Boulevard accepts all debt  
- Mid Atlantic Airways is Born
4. 1994 (NOT SIMULATED)
- Mid Atlantic Airways accepts former US Airways retired 737-200s
- The operation will be known publically as Metrojet, and will be a charter operatin, not a regional carrier.  
- Bouelvard hires first pilots.
- FAA grnats Mid Altantic Airways Part 121 Certicate, flights to start October 1, 1995
- Baltimore, Fort Lauderdale, La Guardia and Washignton National named hubs
5. 1995
- ALPA files information of request eventually supenaing Mid Altantic Airwyas.
- ALPA files injuction to keep the first aircraft from flying. It is denied. 
- ALPA sues Mid Atlantic Airways and US Airlines
- October 1, 1995, first Metrojet flight leaves from Jacksonville, FL to BWI Airport
- The publicity actually makes the airline a hit
- November 5, a 732 experiences total hydraulic falure on approach to BWI. THe aircraft land safely.  Cause determined to be lines were clogged.
6. 1996
- Jan 5, discovery begins in ALPA alwsuite
- Feb 1, a 737-200 experiences uncontrolled engien failure.  Upon landing the aircraf wsas evacuated onto the runway.  Owner of the company not trusting the MX records US Airways gave him, grounded the fleet for a month until the records could be confirmed. Turns out most aircraft had missed MX checks.  The fleet was fixed at cost by US Airways.  
- Feb 24, after the airline Owner demands fixes from US Air, the FAA stepped in and order teh fixes as well.  The timing caused many people to recommend the airline, as it became clear it was prioritizing safety
- March 10, first 10 737-200s return to the skies
- March 18, ALPA lawsuite over scope clause violation goes to trail.
- April 15, remainder of 737 fleet returns to the skies
- April 30, ALPA loses the lawsuite, HOWEVER, the judge states to qualify as an Part 121 Charter operation it much have at least 1 121 flight under its own brand.  The airline lied and stated that was always the planbut this offer required their full attention. The judge gave the airline 2 years.
- June 5, The start of creating a new route network begins.  They airline exploits existing connections to make the carrier start, but notes it cannot use andy US Airways equiment for this.  It is decided that isntead of one flight for legal reasons thaty would opt to make a fully functional airline
- August 5th, Boeing New Airline Program, a very fledgling operation  (Bouelvard is their first customer) is brought in to consult. 
- August 20, at the culstultant's insetence Mid Atlatnic Airline establishes the DBA of Bouelvard Airlines. The Name is eventually establlished as the full corproate name. 
- November 10, Boeing maps out an aggresive growth strategy that will take the company nation wide within months.  Captializing on the success of Metrojet.  Bouelvard will be a slightly preium airlines, providing budget fairs but with first class and amenities of mainline carriers and a first class. 
- December 11, Bouelvard Meets with another consulting grop just to get a second opinion.  They discover a second ariline si gearing up with some billionaires help called New Air. New Air aims to be a premium carrier also with massive trans con plans, and is already in negotiations to pruchase aircraft.  They state that Airbus has already offered them the A320 at a lower than average price.  
- Jan 3: Bouelvard enters aircraft negotiation, the preference is to stick with the 737, but the current line is already claimed out well into 1998. 
- Jan 4: ALPA enters into contract negotiates with the air carriers for their renewal.  
- Feb 10: It is leaked that ALPA plans on trying to Torpedo Metrojet or force it to be brought back in house at US Airways
- June 10:  Boeing's negotiatiosn get tough.  Boulevard opts to travel to France to meet with Airbus.  Were they tour the plant.  However, Airbus prices are far more than Boeings and they are unwilling to negotiate.  However they get the hint that they seem nto to take them seriuously. 
- August 5: they meet with the other consultant group. New Air is having an eaier time negotiating with Airbus.  
- October 10: Boeign offers a compromise, as a number of AA 727s are being change out with 737s they offer then a mixed bag fleet.  Older 727s but with glass cockpits, along with some Fokker 100s.  In a play they ask about aircraft for the rest of their growth plans.  Being ends up promising a sort of frankin fleet of DC-10s, F100, 727s, L1011, MD80s at reduced mx cost.  
- OCtober 20: Boulevard signs onto the frankin fleet but with one addition, brand new 737NG aircraft Intially it the type is left open.
7. 1997
Feb 5: ALPA redfines the scope clause, however, MetroJet cna continue operating as is as the new framework allows it to be defined as a codeshare arrangement 
- July 5: the neo green livary of the airline is estagblished and paint starts.  All new policy also states all incoming used aircraft must undergo D-checks regardless of what its last MX call was
- October 15: In order to make up for laggin sales, Boeing offers Bouelvard the 737-900.  
- OCtober 29:  Bouelvard purchases 100 acres of land in central MD outside of BWI for a new training center 
8. 1998
- Jan 15: First Boulevard Airline branded aircraft takes off from Baltimore.  
- Feb 10: Due to the merger of Boeing and McDonell Douglas, Boeing offers the Airline the newly rebranded 717, and MD-11
- June 14: First returns got back in and the need to increase aircraft frequency became a problem.  Comonality cited.  
- Jul 12: Mid Atlantic Charter switches to a Timeshare based aircraft model, purchasing King Airs, Citantion Jets, and the very first Gulstream. Boulevard Airlines Obsorbs the Flights however. 
- Aug 10:  Boulevard Airlines is in the red, MetroJet is profitable, hwoever US Airways notes it is not profitable enough and they are losing money.  Their finacial advisors see the company has entered into a growth trap.  
- Aug 21: Bouelvard enters into a new growth strategy that will hopefully This means moving into regional, long distance and itnernational travel.  Fuel eefecency was also cited.  We also noticed something interesting.  We were replacing US Airways on a number of routes.  It was decided to seek external information.
- October 1: Boulevard Buys an addtional 1000 acres in MD, and finally starts construction on a Hanover, MD based training faciilty.  Also, they cited that their current Severna Park HEadquarters was far too small THeys tarted looking for new real estate. 
- October 15: after breaking down in a buisness park in Windsor Mill, MD, they opted to buy two abandoned Warehosues and some empty land. The idea would be to build a full operational and modern operations facility.  
- November 12: The City of blatimore offered to give Mid-Atlatnic Charter (Reall Boulevard Airlines) free rent in the closing Montongomery Ward Building they were looking to get people into.  This would become the home of Mid-atlantic Charter.
- December 5: The companies problems were determined.  We littrally ahd everything duplicated.  We were runnign two airliens at the same time and only one was profitable, while the other delt with the debt.  
- December 10: After talking with the reps at New Air, we opted to try again to negotiate with Airbus.
9. 1999
- Janurary 15: Airbus pushes back the meeting for unknown reasons. 
- Feb 14: Bouelvard starts equipping aircraft with GPS tracking . It also equiped all aircraft with ACARS.  Intially this system and overhaul was epxensive, nearly costing $500 a flight.  However it streamlined cocpit operations. 
- Feb 20: Mills coporation enters into a land deal, a 99 year lease in Hanover on 100 acres of land for a new mall.  
- March 7:  THe Reps at New Air break off contact.  
- March 8: American Airliens offloads their order of 50 ATR-500s and another 20 CRJ-700s.  They are aquired a 70% discount.  
- March 30: The company is invited to France again however this comes with a arning from US Airways about the Airbus as they continue to have fleet integration problems with the aircraft.  
- April 1: Bouelvard airlines enters a tentative agreement for the A320, however number of aircraft still needs to be worked out.
- April 15: New Air Changes its name to JetBlue, and they annouce their order of A320s.
- April 30: Boeing contacts Bouelvard offering to reduce cost and offers a new aircraft package at a quarter of what it would cost if the airline reject the Airbus order. 
- March 5: Airbus rejects Bouelvard, stating the DTIs are too high and proper finacing is not secured. 
- March 12: WIthout Telling Boeing, Boulevard accepts the Boeing deal to lease: 20 737-800s unti the 900's are ready, 20 757s, 10 767, and 10 747s.  The 777 was not added due to lack of any availiblity.  Many of these are designed to clear out orders taht are already on the books while the 737 comes from their own lessor.  
- November 18: The original BLVD investor's CEO dies.  The company is Aquired.  They give the company 2 years to be profitable or pay off all debts. 
10. 2000 
Jan 15: Finally Bouelvard starts reciving new Aircraft Although AA offers them some of its old MD-80s which the company accepts to aid in the trasition and allows for the 727s to start being retired. 
NO DATE: Bouelvard also becomes an international airline.
October 5: The new Training facility is complete.
October 10:  The airline entered into negotiations for its first in hosue MX facility in baltimore to lower MX cost. 
Dec 19: A380 is unvealed. 
11. 2001
- Jan 1: The Airline's Investor filled appers in NYC to have the company shut down, with the intent to liquidated.  Bouelvard fileld papers 15 minutes earlier to keep the airline opened, and opted to file a lawsuite as well in MD. The hope was to stall the NYC proceedings.  
- Jan 10: The ploy worked. The court proceedings would be held in MD. This also helped the airline secure a loan from the state to maintain operations and a Federal loan a well, but both were short term operating capital.  
- Feb: The first flight to Narita was a failure. Storms in CA kept the aircraft from flying otu of LAX, but an ANA aircraft timed out, so the CEO of a major Japanese investment firm took Boulevard since he ahd to get home.  Due to the delay, the plane was diverted to HND.  He liked the routing and was aware of their finacial issue. It was discovered the NYC invemetn firm also was heavily invested in Jet Blue, having the signle largest stake in the company. They ahd also informed Airbus not to work with them. The  Tokyo International Air Terminal Corporation who runs Narita and Haneda realized they royally messed up Bouelvard's paper work and they had no terminal space for them in Narita.  They opted to grant the airline an exception to fly to Haneda then to block the flights, but informed them it was until they corrected the situation in Narita. 
- March: The Japanese Investment company was looking for more foreign investment so contacted the BLVD staff.  After one month of showing him that the company was finally moving in the black (but they couldn't buy a new pen or they'd be back in the red) the Japanese firm opted to pay off the companies debt and invest the money the company needed to expand the fleet introduce,better commonality and elimitante the un-prfitable Frankin fleet.  The codnition being if succesfull they would have have to creat a more profitable operation within Japan.  The agreemtn was signed on the 31st.  The NYC investment firm's lawsuite was dropped. 
- April 5: For the first time ever Bouelvard was profitable.  To better compelte with Jet Blue they started an upgrade program and reordered the itniors of all new aircraft.  They would have all leather interiors, seat back entertainment, and Satalight TV.  They opted for a csutom solution from an offshoot of Westing House made in MD.  The sysetem was far lighter and allowed for far more options.  The system would be in every aircraft from the ATR up to the 747 (although region aircraft would have overhead retractable screens not seatback screens). The airline now wanted to opt for a period of respcible expansion
- June 5: The new operations center comes online in Windsor Mill, MD. It has real time trackign of all aircraft and comms through ARCARS.  inspired by a '96 hijacking of an Etheopian flight, they start training for potential hijacking scenariors much to the chargrin of the FAA. 
- July 1: The airline celebrated its first profitable fiscal year even with Metrojet, however US Airways renegotiated Metrojet, putting it almost on a month to month system.  
- Sepetmber 11:
   a. At around 8:40: Bouelvard airlines get's a call from the FAA notifying it that several aircraft are missing.  Ops starts an automated head count of all aircraft
   b. 8:50:People in the lounge notify ops and managment of an aircraft crasing into the WTC.  
   c. 8:55: The head count is complete/ FAA notifies the airline to get itno positive contact with all aircraft. Company decides to reheck and ask for comfirmation of details, implimenting hijacking protocal. 
   d. 9:00: Bouelvard calls up the FAA and NORAD and tells them positive contact with all aircraft and all are in control. Both NORAD and FAA seem to think of the crash as a 1 off, but most airlines have only contact a hand full of their aircraft. 
   e. 9:03: WTC South Tower Hit.
   f. 9:10: Without authorization from the FAA or NORAD, BLDV owner orders all aircraft onto the ground immediatly and suspends all deaprtures.  They then call the ATC and order them to cancel all flight plans and inform any departing aircraft to return.  OPS teams send out commands for all aircraf to proceed to the nearest commercial airport. 
   g. 9:20: First aircraft start landing in the fleet.
   h. 9:26: All departures banned in the US by FAA/NORAD
   i: 9:37:  DCA crews report explosion from Pentegon.  A hit is confirmed 5 minutes latter. 
   g. 9:40: BLVD gates at  certain airports, like BWI fill up, and crews are ordered to juggel aircraft at teh gates. DCA is closed and flights rerouted to BWI or Richmond. 
   h. 9:45: US Airspace Closes
   i. 9:50: International flihghts inbound to the US from Europe are sent to Iceland or Toronto. Flights from Asia are order to turn back and land in japan. 
   j. 10:30: 100% of the Boulevard Domestic fleet is on the ground. 
   k. 11:15: 100% of the Bouelvard fleet world wide is on the ground. 
   l. 3:00PM: Busses and cars are auhorized for crew, and about 40% of the fleet is abandoned at out stations or non-desitnations, with the intent to rehome all crew who are located domestically within 24 hours. We try to put crews in luxury downtown hotels as we have no idea how long they will be there.  
   m. 1100PM: Airline ops center goes to reduced staffing and the airline is officially closed. 
- September 14: "Reoppening"  BLVD used Mid Atlantic Charters to fly crew back the stranded aircraft and stared flying some back to hubs.  We opted to just do a full reset.  Passengers levels were nearly 70% intially.
- September 16: Flights to desitnations were restarted, but Load factors were at 50%, so we opted to do a phased restart, and rollign furloughs, a plan that was devised during the  this was actually fully devised during the fear of brnkrupcy.  The airline was dropped to 60% intially with crews flying around 60% of their normal schedule.  No new aircraft orders were canceld, instead opted to park them in the desert.  DCA remained closed. 
- September 23: USAir cuts Metrojet, although operations are allowed to continue.
- October 4: DCA reopens, however due to slot restrictions, and prioritztation of the larger carriers, Metrojet and Boulevard are not allowed to fly itnially.  Not ATRs were allowed nor CRJs. Capcity is cut by 60%, a 1250 mile distance rule is put in place for DCA. 
- Dec 1: Metrojet contract was pulled, in arbitration, Bouelvard Airlines was paid out $10 million for the end of the contract with teh rest of the value coming from the Metrojet gates, aircraft (the 737-200) and equipment.  USAirways filed for bankrupcy.  That started a dangerous plan.  The airline would try to cannibalize ride share by feeding off the death of the legacy carriers. 
- Dec 15: The 737-200s were parked, replaced as 717s and 737-900s came online. Bouelvard gave US Airbus its ten million back for 10% of their network and their A320 and A340 order. They agreed. 
- Dec 20: As the review of 9/11 occured a news story on 60 Minutes About Boulevard Airline's responce during 9/11 caused a bump in sales. It was also mentioned by Preisdent Bush.
- Dec 22: Shoe Bomber on a Delta flight causes massive flight delays. 
22. 2002
- SARS outbreak in Asia Pacific causes flights to Hong Kong and China to drop 80%, hits Japan and Korea traffic by a -15% reduction
- Februrary 12: DCA starts a slow roll in procedure that amounts to return to 100% by the end of the year. Boulevard Bogards its way into the front of the line, stating that it has been nearly unable to operate.  The MWAA denies them.  
- Feb 20: Boulevard forms a new in house legal group. While the group was formed due to a incident in June 2001, a high turbulence event that caused several children to go flying in the cabin after repeted warnigns to sit down.  They files suite against the MWAA that day over DCA landing rights.
- March 1: The case against the MWAA last all but 4 hours and Boulevard wins hands down. It will get 1 out of every 2 slots until it returns to pre-9/11 capacity.
- April 5: In order to save weight in order to cut cost, we remove on row of seats and re-align the cabin of the aircraft in overnight proceedures.  This gives a 34" pitch to coach. 
- June 10: After the lawsuite against Bouelvard by the youth Dirt Bike ended with a Boulevard victory, they decided to start offer a program to atract youth groups. It would offer free or reduced rates to youths on official travel. The hope is to attract their parents. It would be open to any group as long as if officially registers with the company. 
- July 6:  While in France dealing with the Airbus order, Airbus conviences the airlines to put a deposite on the A380.  The real idea was to try to secure more orders of A320 for a dual aircraft fleet, but we only stettle on 12 A320s and 6 A340s. A320s are used as hub connectors in the US north east while the A340's based on flights to Europe and Israel.
- Aug: US Air files for bankrupcy
- Aug: Bahamas Air Files for Bankrupcy. 
- August 28: The Summer vacation season saw capacity return to 90% of normal.  Boulveard finishes a massive retraining program, and retrofittign the fleet with new armored doors.  Bouelvard and Southwest get together and design a secureity door that can be pulled.  All flights duct tape, tasers.  New security training the airline opted to implement for self defense, a permentant self defense team is hired for training. Security cameras are installed in key locations in the aircraft  
- November 10: SWA and Bouelvard combine resoruces and enter into a massive fuel hedging deal.  
- November 15: Capacity has managed to return up to 75% of pre-9/11 capacity.  
- December: Sonic Cruiser is canceled. Boeing invtes Bouelvard to work with them on a new aircraft. 
- December: The Bahamian Government Offers a loan and sale of Bahamas ais to us management hopigng comgining it with the larger US airlines will cut cost.  Boulvard is the only carrier capable.  Bahamas sells the airline for $150 million 90% of the loan of the loan it over 50% of the operations and every attempt to hire local pilots is made.  Boulevard considers it a challenge, and potentially practice. 
- December: United Declares Bankrupcy. 
23. 2003
- Jan: USAirways cuts about 30% of their East Coast flights.  
- Jan: United Cuts around 40% of flights.  
- Jan 29: 7E7 is introduced. Bouelvard becomes a launch customer. 
- Feb 1: with help of thier creditor Boulevard manages to big on USAirways, United and NWA (NWA looked to cut cost) assets.  This increase fleet size almost by double, pax capacity by June goes up to 2x pre-9/11 levels.  We also purchase a large share of DTW's new terminal making DTW a hub. We also obtained a number of aircraft including the 777, 757s, and a few 747-400s as well as new aircraft slots. 
- June 10: With the realization the max profit will not be in Airline buisness, the company opens Bouelvard Holdings.  The Holding company goes into real estate holdings, and begins purchasing land nation wide. However, we do bring in a consultant to try to operate it as a small company not as a faceless conglomerate. 
- July 5: NWA, Continental, and Bouelvard enter into a resource sharing agreement. This is not a coode share agreement, just ground resources and resources at international flights. 
- August 5: While Boulevard traveled to Toulous to settle A380 order possibly became a launch customer and potentially expend Airbus fleet, Airbus pruposly left them waiting for 5 hours , and pushed Singapore to take the launch position. They overheard they purposely ignored them in protest due to the War in Iraq.  Bouelvard laid them out opted to finish the A380 order, but refused to extend the Airbus fleet. 
- August 25:  Bouelvard estimates the nearly 12,000 youth groups and nearly 20,000 youths have traveled for free, however it worked and this yeilded more capacity, as well, it was a massive tax right off leading to some taxes to be tax free.  It also gives the company a good image. 
- November 5:  PResident Bush stated that moving troops back from Iraq for the Holidays would be impossible.  So on a dare the airline got some pilots willing to pull of several troop repatriation flights to Germany.  Using 4 747s, the flights were pulled off.  The DOD then activated the Civil Reserve Air Fleet.  The Process ends up continuing n the Fed's dime at 4x cost until permentant charter operators can be chosen in Janurary the following year
- December 31:  Bouelvard celebrates its first holiday at full capacity, actually one of the few airlines to grow after 9/11 2x in size.  
24. 2004
- Bahamas Airlines declare success with the Bahamas Air Buy out.
- Aloha Airlines and ATA go bankrupt.  In at attempt to get more west coast services, Boulevard purchases the Aloha as one operational entity.  Managment is released along with much of the operations, but everything is combined, however the name is kept and all flights operate under the Aloha name brand with a unique culture aspect. Boulevard waits till ATA goes to auction and purchases several 757s and 2 L1011s.  
- Airline produces a new model that reforms around the 737 at its core.  
- 7E7 becames 787.   Airbus clones the aircraft as the A350, but will redesign it as the A350XWB after the intial design flops.
- Boulevard becames the first operator of the E-175 in the US, and is the launch customer for the 737-900ER
- A number of major hurricanes hit Florida back to back. 
- SARS airline recovers
- US Air files for bankrupcy again.  The airline is too big for Boulevard to aquire. 
- Dec 6: Tsunami in the Indian Ocean.  Boulvard airlines offers 2 747s to the US Red Cross at cost.  
- Due to fleet cuts, insufficient seats to fly to Greece are present.  So Boulevard host large charter flies to get athletes to the Olympics as charter and the airlines tries to redirect more flights getting special clerance. 
25. 2005
- A380 first flight
- B777-200LR first flight
- B747-8 launched
- Jet Blue obtains E190, Boulvard opts for the E195
- AUG:  Hurricane Katrina hits New Orleans.  The airport is close for several weeks as the  runways are partially floored.  4 days latter Bouelvard opts to attempt to land aircraft on the half closed runways. Boulevard operates emergency evacuation flights in concert with the US military to Houston, but due to the condition of MSY can only operate during the day. 
- September: The airline wins the presidential Freedom award for its service in Katrina
- September 14: Delta and Northwest file for bankrupcy.  Despite not being involved, Bouelvard opts to start investigating more ways to save money, stay lean, and look for alternate investments.  
- Sep: US Air and America West merge.  To cover the merger they need to build artificial competition on shared routes, so opts to bring in Boulevard give them capcity in exchange for relgulators approving the merge
26. 2006
- Terrorist attack is thwarted in Europen, but leads to liquids being banned from flights
- Boulevard takes first 777-200LR
- Airline takes first 737-900ER
- Hooters Corp Sells the company to Boulevard for practically nothing ($5 million).  The idea was to use their 737s to merge into the carrier imemdaitly, but this actually takes 2 years to do surpsingly, the Novelty airlines actually staying pretty popular. 
- After a year check, the company comes up with a tree prong plan to keep cost fix for the next 15 years even with high fuel cost. Bouelvard Holdings became necessary to keep the company afloat and becames an investment firm.  Start using real estat through BRAC as much of its real estate in MD is targeted by the DOD for expansion projects. Also looks into aquiring a refinery. Invest in Biofuels, adopts a program of constant fleet upgrades and commissions a aircraft seat manufacturer to create a new composite seat.  Also save money by keeping tech managment inhouse.  
- Boulevard Japan is register in Japan, but needs to go through review by Japanese regulators.
- Bouelvard is named the official airline of the USIOC.  
27. 2007
- Japanese regulators appove Boulevard Japan, and 12 smaller operators are obsorbed overnight.  Many in Japan decry the mergers, especially by a foriegn company.  Boulevard Japan is formed.  However the Boulevard owner sees the situation with the airlines.  On the jets the service is excelent, behind the scenes they are behind, and wasting tons of money, and only make money due to subsidies that were running out.  The airline's management is refocused in Nakai, JP with sims located in Kyoto,JP primary classrooms in Tokyo. US quality of life policies are put into place along wiht heavy technological adoption. It is not accepted well. especially when it comes to the stamps
- Boulevard starts working with a large finacial services company in an attempt to build a Pension program
- ALPA pushes to enter Bouelvard 
- 787 rolls out
- A380 first delivered to Singapore.  Bouevlard recieves their aircraft in July.  The Aircraft is based out of Baltimore.  It ends up flying from BWI to HND...this is strategic as the aircraf at the time was too large to fly out of Narita. The route is shared with a 747.  We beat Singapore into sevice by a matter of days. We also push for the creation of an additional terminal.
- US - EU Open Skies agreement opens up Europe to Boulevard.
- Richard Branson's new Virgin America signs a 10 year MX agreement with Boulevard Airlines. They will also lease training space. 
- Boulevard's new coach class chair is 30% lighter and actually exceeds standard airliner seats in safety, but its 10% more expensive.  The airline opts to make them standard acorss the fleet. NOTE: as standard metal bench of 3 seat weighs around 90lbs, meaning ours weighs 63lbs.  The new entertainment system is also lighter.
- Housing buble hits in May, shashing demand in the US 10-15%
28. 2008.
- Boulevard Airlines invest heavily in Oil and redoes its hedge constracts.  
- Bouelvard holdings becomes heavily diversified, gaining 
- Oil hits $147 a gallon, trips to vacation destination and buisness trips falls 15%, making the total drop 30% with last years drop
- Boulevard's liscence on the Hooters name ends, forcing the airline to finally fully absorb the company
- Airlines add baggage charges, but Boulevard opts not to do this.  It quickly became 1 of 3, the others being Jet Blue and Southwest. Boulevard has its first SuperBowl commerical with the surviving members of the movie Airplane in starring roles.
- MD-88s retired from fleet.  
- Pilots vote against ALPA unionization
- After looking at the aircraft, the airline decides to add a row back, but redsigns its custom seat, and moves the rear bulk head back just enough to simulate the 34" pitch in coach.  
- Wiht a repuationas a fixes, an EU invetstor group asks Boulevard to take over two of their investments, Air Tahiti Nui which was nearing bankrupcy and Alitalia.  They agree...this is helped by the April Delta NWA merger. As witht eh USAir, America West Merger, they need compeition along ther route, so they sell Boulevard elements of the company at rock bottom prices including next to half of NWA's 757-300s, this will make 100% of the 757-300 aircraft in Boulevard's fleet. NWA would also sell Boulevard Northwest Orient Airlines.
- Aug/Sept: Houston area airports shut down for 2 weeks due to back to back hurricanes
- Oct: BLVD unveals private first class full lie down seats in international, and side by side buiuness seats, as well as lighter seat back entertainment. Original buisness class seats sent to domestic fleet.
- Dec: rolling furloughs begin due to lack of demand.  Upper managment takes a 50% pay cut. Emploeeys reduced service time reduced to 30% of normal
29. 2009
- Continental leaves SkyTeam, leaving Boulevard alone in the aliance, so SkyTeam goes under. 
- Boulevard enters intoo a short team lease of 4 years the Russian IL-96 and the ANT148.  
- Continental and United merge.  United refuses to give anything to BLVD, recongizing they are getting to powerful. 
- Spring H1N1 virus causes traffic to drop to 50% normal.  Boulevard increases rolling furloughs to 45%. 
- Boulevard notes that retirements are approaching, so opts try to partner with flight schools.  The furloughs and parked planes accelerate new first class, buiness, an coach.   
- Boulvard standardizes hiring rles for FOs locking in reqrirements at 21 and 500hrs TT
- Boulevard opts to increase pay period to pilots entire work day not just flight time. (note: this will be defines as duty day)
- Colgan crash 3407 crash. 
30. 2010
- Alitalia senior pilots reject restructuring plan at the behest of ourside force
- April 10- late March: Eyjafjallajökull Volcanic Eruption shut down European airspace.  in late April,it was determined lowers European Airpots were still reported clear of upper level ash.  So using our trijets and 747s Boulevard flew its flights to Southern Europe via the central atlantic ocean and not the typical norther route add 2 hrs to the flights.  
- May: Eropean flights return t normal, Boulard, returns to 80% capacity allowing furloughs to return to 90% work schedual and exec pay to return to normal. United and Continental merger concludes, but they must surrender slots at IAH, LAX, and  EWR.  ONly Boulevard is able to take the slots.  SWA doesn't want them. 
- Boulevard becomes launch customer for the Cseries. 747-8f flies. A320neo launches
- Nov: 787 test fleet delayed
- Dec, NYC an BOS airports shut down for 2 weeks due to massive blizzards as well as LON area airports
31. 2011
- after lobbying for a 737 replacement BLVD makes its objections know as Boeing caved to AA, SWA, United anr RyanAir to build a 737"neo"(aka 737MAX).  
- oh AA files for bankrupcy
- 747-8i takes first light and the -8f deliveries start
- March 11, 2011: An Earthquake and Tsunami hits Japan.  A 737-900ER barely 2 years old is caught trying to evacuate people from Sendai at the last minute get's caught in the flood waters, but manages to stay stable.  Over the next 2 months, intially canceling passenger flights and sending over emergency rescue crews Bouelvard deploys several aircraft with the US Redcross. After a confrentation with the local authorities which have been leaaving people in forzen shelters without power, citing no availible shelers.  Boulevard steps in, and forces the Handea international terminal open, as it just got its occupency certification, staffed it with food and resources, organized the place and incomeping people to provide services and created a small town for the survivors and began evacuations with the JSDF and the US forces authorized to operate.  
- Boulevard returns to 100% capcity and starts to regrow at an acclerated rate.  By December the airline is no longer dependent on the Holdings company to say healthy
- Boulevard fights the 1500 hour rule. However ALPA comes in and uses their money and crying families to get the rule approved into law.  
- May  10: The 737 stuck at Sundai, is eventually transported by barge to HND. It is determined with will need heavy work, but its opted to retrofit the aircraft.
32. 2013
- Jan: JAL uses the wrong fuse on a 787 causing a fire in the Li batteries  All aircraft are delayed to May
- A350 first flight.  However, to sell them Airbus thretens Airlines how cancel their A380 orders to buy the A350.  However, they get rumors of Airbus offering execs cash gifts for buying the craft. 
- BLVD takes its last of its original 16 A380s.  
- CSeries first flight
- AA and USAir Merger.  
- After nearly a year as a shelter, the last civilians living in Haneda international terminal return home. This clears the terminal to be completed
- Boulevard travels to Dubai and investigates Emirates.  While no codeshare deal is hathed it becomes clear to where the airline's next step is.  
- IL-96 return back to Russia, but Boulevard opts to purchase the AN148s but they are recertified with western built engines.  
33. 2014
- Jan: Massive polar vortex messes with Aircraft systems casuing a number of cancelations. 
- The MD-11 retirement is annouced.  It will be the last trijet in the fleet.
- July 17: Ukranian airspace closed after Russia shoots down a passenger aircraft. 
- Oil crashes from $115 a barrel in June to $60 in December 
- Alitalia restructure stalls once more as the Senior pilots file a lawsuite. 
- A320neo first flight 
- Boulevard aims to transform the airline into a premium carrier to compete against Emirates and Singapore. 
- Boulevard opens Bouelvard Cargo. It begins converting 747-400s to cargo aircraft, replacing them with 747-8s and replaces 767-400s with 787s and the 767s are converted to cargo aircraft. FedEx enters into a deal to purchase the MD-11s. 
- 1500 hour rule takes affect.  FAA surprises airlines back not allowing pilots already hired too be grandfathered in. 
34. 2015.
- BLVD teams up with Gulfstream after Mid-Atlantic CHarter sets a record order for the aircraft, to design a brand new interior for their aircraft.  
- Boulevard breaks massive record profits, and becomes the most profitable airline world wide.  Oil droped to $50 a barrel. 
- Boulevard's Japanese investor forces the company to put down an order for the MRJ



