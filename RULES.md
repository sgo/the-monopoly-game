# Belgian Monopoly — Official Rules

This document describes the official rule set for the Belgian edition of Monopoly, as
modeled by this project. Sections marked **(project scope)** describe only what is
currently implemented in `the-monopoly-game-specs`. The remaining sections are taken
from the official Hasbro/Parker "STANDARD MONOPOLY" instruction booklet (Dutch-language,
ref. 00009104, © 2001 Hasbro Inc., distributed in the Benelux by Hasbro B.V.) — the same
rule text used for the Belgian retail editions of this period.

## Overview

MONOPOLY is the game in which real estate must be bought, rented, or sold as
favourably as possible so that a player's wealth increases — whoever ends up
richest wins. Players start at "START" and move their pawn, on their turn, as many
squares around the board as shown by the dice. Landing on unsold land lets a player
buy it from the bank; if they decline, the bank auctions it immediately to the
highest bidder. Players who own land collect rent from opponents who land on it;
rent increases as more houses and hotels are built, so building is advantageous.
To raise cash, a street can be mortgaged to the bank. Instructions on Community
Chest or Chance cards must always be followed. Sometimes a player must go to jail.

**Goal**: be the only player left who is not bankrupt.

## Equipment

Per the official instruction booklet:

- 1 game board
- 10 pawns
- 28 title deed cards
- 16 Chance cards
- 16 Community Chest cards
- 1 set of special MONOPOLY money
- 32 houses
- 12 hotels
- 2 six-faced dice

## Setup

1. Divide the houses, hotels, title deeds, and money (sorted by value) among the
   compartments of the box.
2. Sort the Chance and Community Chest cards, shuffle each stack, and place them
   face-down on their respective spaces on the board.
3. Each player chooses a pawn and places it on "START".
4. **The banker and the bank.** Choose which player runs the bank. If there are more
   than five players, that player may act solely as banker. The bank gives each
   player **€1500** in the following notes:
   - 2 × €500, 4 × €100, 1 × €50, 1 × €20, 2 × €10, 1 × €5, 5 × €1

   Besides the cash, the bank also holds the title deeds, houses, and hotels until
   players buy them. The bank pays salaries and bonuses, lends players money against
   mortgages when needed, and collects all taxes, fines, loan repayments, and
   interest. When land is auctioned, the bank acts as auctioneer. The bank can never
   go "bankrupt" — if it runs out of money it may pay players using paper IOU notes.
5. Players take turns rolling the dice. The highest roll goes first, then play
   continues clockwise.

## Taking a Turn

On your turn, roll both dice and move your pawn that many squares in the direction
of the arrow. The space you land on determines what happens next. Multiple pawns
may occupy the same square at once. Depending on the space, you may need to:

- buy the land your pawn is standing on,
- pay rent (if another player owns that land),
- pay tax,
- draw a Chance or Community Chest card,
- go to jail,
- freely park,
- collect €200 salary, or
- simply be "just visiting" in jail.

### Rolling doubles

If you roll doubles, move your pawn and resolve that space as normal, then roll and
move again. If you roll doubles three times in a row, you must go directly to jail
instead of taking the third move.

## Passing / Landing on "START"

- Every time you land on or pass "START", you collect **€200** salary from the bank.
  You can earn €200 twice in one turn — for example, if you land exactly on "START"
  and then draw a Chance/Community Chest card reading "Advance to START". 

## The Board

The board is a loop of 40 spaces: streets, corner spaces, stations, utilities, and
tax/card spaces, grouped into 8 colour groups (cities). Streets are named after real
streets and cities in Belgium, drawn from both language communities plus Brussels.
The layout below is taken directly from the current bilingual (Dutch/French) Hasbro
Gaming retail edition (ref. C1009), reading clockwise from "START":

| # | Name (NL / FR)                                     | Colour group | Price |
|---|-----------------------------------------------------|--------------|-------|
| 0 | START / DÉPART                                       | —            | —     |
| 1 | Rue Grande (Dinant)                                  | brown        | M60   |
| 2 | Algemeen Fonds / Caisse de Communauté                | —            | —     |
| 3 | Diestsestraat (Leuven)                               | brown        | M60   |
| 4 | Inkomsten Belasting / Impôts sur le revenu           | tax          | pay M200 |
| 5 | Noord Station / Gare du Nord                         | station      | M200  |
| 6 | Steenstraat (Brugge)                                 | light blue   | M100  |
| 7 | Kans / Chance                                        | —            | —     |
| 8 | Place du Monument (Spa)                              | light blue   | M100  |
| 9 | Kapellestraat (Oostende)                             | light blue   | M120  |
| 10| Op Bezoek / Simple Visite (Jail / Just Visiting)     | —            | —     |
| 11| Rue de Diekirch (Arlon)                              | pink         | M140  |
| 12| Elektriciteitscentrale / Centrale Électrique         | utility      | M150  |
| 13| Bruul (Mechelen)                                     | pink         | M140  |
| 14| Place Verte (Verviers)                               | pink         | M160  |
| 15| Centraal Station / Gare Centrale                     | station      | M200  |
| 16| Lippenslaan (Knokke)                                 | orange       | M180  |
| 17| Algemeen Fonds / Caisse de Communauté                | —            | —     |
| 18| Rue Royale (Tournai)                                 | orange       | M180  |
| 19| Groenplaats (Antwerpen)                              | orange       | M200  |
| 20| Gratis Parkeren / Parc Gratuit (Free Parking)        | —            | —     |
| 21| Rue St-Léonard (Liège)                               | red          | M220  |
| 22| Kans / Chance                                        | —            | —     |
| 23| Lange Steenstraat (Kortrijk)                         | red          | M220  |
| 24| Grand Place (Mons)                                   | red          | M240  |
| 25| Buurtspoorwegen / Tramways Vicinaux                  | station      | M200  |
| 26| Grote Markt (Hasselt)                                | yellow       | M260  |
| 27| Place de l'Ange (Namur)                              | yellow       | M260  |
| 28| Watermaatschappij / Compagnie des Eaux               | utility      | M150  |
| 29| Hoogstraat (Brussel) / Rue Haute (Bruxelles)         | yellow       | M280  |
| 30| Naar de Gevangenis / Allez en Prison (Go to Jail)    | —            | —     |
| 31| Boulevard Tirou (Charleroi)                          | green        | M300  |
| 32| Veldstraat (Gent)                                    | green        | M300  |
| 33| Algemeen Fonds / Caisse de Communauté                | —            | —     |
| 34| Boulevard d'Avroy (Liège)                            | green        | M320  |
| 35| Zuid Station / Gare du Midi                          | station      | M200  |
| 36| Kans / Chance                                        | —            | —     |
| 37| Meir (Antwerpen)                                     | dark blue    | M350  |
| 38| Extra Belasting / Taxe de Luxe                       | tax          | pay M100 |
| 39| Nieuwstraat (Brussel) / Rue Neuve (Bruxelles)        | dark blue    | M400  |

Notes on this layout:
- Each colour group has 2 or 3 streets: brown (2), light blue (3), pink (3), orange
  (3), red (3), yellow (3), green (3), dark blue (2) — 22 streets in total.
- 4 stations at M200 each, and 2 utilities at M150 each, matching the standard
  Monopoly structure.
- "M" is the board's own in-game currency symbol printed on the money and title
  deeds (the special MONOPOLY money mentioned under Equipment), not necessarily a
  literal abbreviation for euros.
- Rent tables, house/hotel construction costs, and mortgage values per street are
  printed on each title deed card but are not legible from the board.

## Rent, House Costs, and Mortgage Values

| Street | Price | House cost | Rent (unimproved) | Rent (1 house) | Rent (2 houses) | Rent (3 houses) | Rent (4 houses) | Rent (hotel) | Mortgage |
|---|---|---|---|---|---|---|---|---|---|
| Rue Grande (Dinant) | M60 | M50 | M2 | M10 | M30 | M90 | M160 | M250 | M30 |
| Diestsestraat (Leuven) | M60 | M50 | M4 | M20 | M60 | M180 | M320 | M450 | M30 |
| Steenstraat (Brugge) | M100 | M50 | M6 | M30 | M90 | M270 | M400 | M550 | M50 |
| Place du Monument (Spa) | M100 | M50 | M6 | M30 | M90 | M270 | M400 | M550 | M50 |
| Kapellestraat (Oostende) | M120 | M50 | M8 | M40 | M100 | M300 | M450 | M600 | M60 |
| Rue de Diekirch (Arlon) | M140 | M100 | M10 | M50 | M150 | M450 | M625 | M750 | M70 |
| Bruul (Mechelen) | M140 | M100 | M10 | M50 | M150 | M450 | M625 | M750 | M70 |
| Place Verte (Verviers) | M160 | M100 | M12 | M60 | M180 | M500 | M700 | M900 | M80 |
| Lippenslaan (Knokke) | M180 | M100 | M14 | M70 | M200 | M550 | M750 | M950 | M90 |
| Rue Royale (Tournai) | M180 | M100 | M14 | M70 | M200 | M550 | M750 | M950 | M90 |
| Groenplaats (Antwerpen) | M200 | M100 | M16 | M80 | M220 | M600 | M800 | M1000 | M100 |
| Rue St-Léonard (Liège) | M220 | M150 | M18 | M90 | M250 | M700 | M875 | M1050 | M110 |
| Lange Steenstraat (Kortrijk) | M220 | M150 | M18 | M90 | M250 | M700 | M875 | M1050 | M110 |
| Grand Place (Mons) | M240 | M150 | M20 | M100 | M300 | M750 | M925 | M1100 | M120 |
| Grote Markt (Hasselt) | M260 | M150 | M22 | M110 | M330 | M800 | M975 | M1150 | M130 |
| Place de l'Ange (Namur) | M260 | M150 | M22 | M110 | M330 | M800 | M975 | M1150 | M130 |
| Hoogstraat (Brussel) / Rue Haute (Bruxelles) | M280 | M150 | M24 | M120 | M360 | M850 | M1025 | M1200 | M140 |
| Boulevard Tirou (Charleroi) | M300 | M200 | M26 | M130 | M390 | M900 | M1100 | M1275 | M150 |
| Veldstraat (Gent) | M300 | M200 | M26 | M130 | M390 | M900 | M1100 | M1275 | M150 |
| Boulevard d'Avroy (Liège) | M320 | M200 | M28 | M150 | M450 | M1000 | M1200 | M1400 | M160 |
| Meir (Antwerpen) | M350 | M200 | M35 | M175 | M500 | M1100 | M1300 | M1500 | M175 |
| Nieuwstraat (Brussel) / Rue Neuve (Bruxelles) | M400 | M200 | M50 | M200 | M600 | M1400 | M1700 | M2000 | M200 |

Stations (Noord, Centraal, Buurtspoorwegen, Zuid) each cost M200 and mortgage for
M100. Rent depends on how many stations that owner holds: M25 (1 station), M50 (2),
M100 (3), M200 (4).

Utilities (Elektriciteitscentrale, Watermaatschappij) each cost M150 and mortgage
for M75. Rent is calculated from the dice roll, per the [Utilities](#utilities)
section below.

## Buying Land

Landing on unsold land (a space whose title deed no one owns yet) gives you first
right to buy it from the bank at the price printed on the space. If you buy it, pay
the bank and take the title deed, placed face-up in front of you. If you decline,
the bank must immediately auction that land, starting at whatever price a player is
willing to offer. The player who declined to buy may also bid in the auction.

## Rent

If you land on land owned by another player, the owner is entitled to rent. The
owner must ask for rent before the next player rolls the dice — otherwise it is
generally accepted to be waived for that landing. The amount owed is shown on the
property's title deed and depends on how much has been built there.

- If a player owns every street of one colour group (a "monopoly"), they may charge
  **double rent** on those streets while they remain unimproved. Double rent may not
  be charged if any street in that colour group is mortgaged.
- Rent for streets with houses or a hotel is higher than for unimproved land (see the
  title deed).
- No rent may be charged for streets that are mortgaged.

### Utilities

Landing on a utility lets you buy it, if unsold, the same way as other land. If
another player owns it, they're entitled to rent calculated from the dice roll that
brought you there:
- one utility owned by that player: rent = **4 ×** the dice roll,
- both utilities owned by that player: rent = **10 ×** the dice roll.

If you arrived on the utility as the result of a Chance/Community Chest instruction,
roll the dice at that point solely to determine the rent owed.

### Stations

The first player to land on a station may buy it; otherwise the bank auctions it (a
player who declined to buy may still bid). If already owned, pay the amount shown on
the title deed — the rent depends on how many stations that owner holds.

## Chance and Community Chest

A player landing on either space draws the top card of the corresponding deck. Cards
may instruct the player to:
- move their pawn,
- pay money (e.g. a tax),
- receive money,
- go to jail, or
- leave jail without paying.

The instruction must be carried out immediately, after which the card is placed
face-down at the bottom of its deck. A "Get Out of Jail Free" card may be kept until
used or sold for an agreed price. Note: if a card instructs you to move and this
takes you past "START", you collect €200 — unless the card sends you directly to
jail, in which case you do not pass "START" and collect nothing.

### Chance Cards

1. Ga door naar Nieuwstraat (Brussel) / Rue Neuve (Bruxelles).
2. Ga door naar START (Ontvang M200).
3. Ga door naar Grand Place (Mons). Als je langs START komt, ontvang je M200.
4. Ga door naar Rue de Diekirch (Arlon). Als je langs START komt, ontvang je M200.
5. Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.
6. Ga door naar het dichtsbijzijnde station. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, betaal je de eigenaar dubbel de huurprijs.
7. Ga door naar het dichtsbijzijnde nutsbedrijf. Indien nog niet verkocht, mag je het kopen van de Bank. Indien verkocht, rol de dobbelsteen en betaal de eigenaar tien keer de gerolde waarde.
8. De bank betaald je een dividend van M50.
9. Verlaat de gevangenis zonder te betalen.
10. Keer 3 stappen terug.
11. Ga naar de gevangenis. Passeer niet langs START, je ontvangt geen M200.
12. Renoveer al je eigendommen. Je betaald M25 voor ek huis. en M100 voor elk hotel.
13. Boete voor te snel rijden. Betaal M15.
14. Ga door naar Noord Station / Gare du Nord. If you pass START, collect M200.
15. Je bent verkozen tot de nieuwe voorzitter. Betaal elke speler M50.
16. Je lening is afbetaald. Je ontvangt M150.

### Community Chest Cards

1. Je maakt elke week tijd vrij voor je bejaarde buurman — Je hebt geweldige verhalen gehoord! Je ontvant M100.
2. Je organiseert een groep om de voetpaden op te ruimen. Je ontvangt M50.
3. Je bent vrijwilliger bij het rode kruis. Er waren gratis koekjes! Je ontvangt M10.
4. Je koopt wat koekjes op het schoolfestival. Lekker! Je betaald M50.
5. Je hebt een puppy gered — en je voelt voldoening! Verlaat de gevangenis zonder betalen. Bewaar deze kaart tot je ze nodig hebt. Je kan de kaart ook ruilen of verkopen.
6. je organiseert een buurtfeest zodat de mensen elkaar beter leren kennen.
   Je ontvangt M10 van elke speler.
7. Luide muziek diep in de nacht? Je buren zijn boos. Ga naar de gevangenis. Je komt niet langs start START. Je ontvangt geen M200.
8. Je helpt jouw buur met haar boodschappen. Ze bedankt je met een lekkere lunch! Je ontvangt M20.
9. Je helpt met het bouwen van een nieuwe speelplaats!
   Je ontvangt M100.
10. Je speelt de hele dag met de kinderen in het kinderhospitaal.
    Je ontvangt M100.
11. Je ging naar de car wash inzamelactie van de school — Maar je vergat de ramen te sluiten! je betaald M100.
12. Net wanneer je denkt dat je geen stap verder kan, bereik je de finish! Ga door naar START. je ontvangt M200.
13. Je helpt je buren hun tuin opruimen na het onweer. Je ontvangt M200.
14. Je vrienden in het dierenasiel zijn je dankbaar voor je gulheid.
    je betaald M50.
15. Je had beter deelgenomen aan het renovatie project — je zou waardevolle vaardigheden geleerd hebben! Betaal M40 voor elk huis wat je bezit. M115 voor elk hotel.
16. je organiseert een wafelbak voor de plaatstelijke school. Je ontvangt M25.

## Taxes

Landing on "Income Tax" requires paying the indicated amount to the bank.

## Free Parking

Landing here has no penalty — you simply wait until your next turn. There is no
built-in reward either: you may still collect rent, build, buy, and sell as normal.

## Jail

You are sent to jail when:
- your pawn lands on the "GO TO JAIL" space,
- you draw a Chance/Community Chest card reading "GO DIRECTLY TO JAIL", or
- you roll doubles three times in a row in one turn.

Going to jail ends your turn immediately; you do not collect €200, since you don't
pass "START" — you go straight to jail.

To leave jail, you may:
- pay a €50 fine and continue playing on your next turn, or
- buy a "Get Out of Jail Free" card from another player and use it, or
- use a "Get Out of Jail Free" card you already hold, or
- wait, attempting to roll doubles on each of up to 3 turns in jail — rolling doubles
  releases you and you move that number of squares. After three turns without
  doubles, you must leave jail and pay the €50 fine before moving your pawn.

Players in jail may still collect rent as normal, provided their land is not
mortgaged. If you land on the jail space without being sent there, you are "just
visiting" and simply continue as normal on your next turn.

## Houses

A player may only start building once they own every street of one colour group (one
city). Building houses increases the rent opponents must pay when they land there.
The price of a house is shown on the relevant title deed. Houses may be bought at any
time, but must be built evenly: a street may not receive a second house until every
street in that colour group has one house, and so on up to the maximum of 4 houses
per street. Houses are sold back in the same even manner. Buying and selling may
happen at any point in the game, as many times as a player wishes and can afford.

No building is allowed in a colour group while any street in it is mortgaged. If a
player owns a full colour group but only one or two of its streets are built up, they
may still charge double rent on the remaining unimproved streets of that group.

### Shortage of houses and hotels

If the bank has no houses left, you must wait until another player trades some in or
sells some back to the bank. A hotel may also be sold and exchanged for houses when
houses are otherwise unavailable. If houses or hotels are limited and two or more
players want them, the bank must auction them to the highest bidder, starting at the
lowest price printed on a title deed.

## Hotels

A player wishing to build a hotel must first have 4 houses on every street of that
colour group. A hotel is bought the same way as a house, costing 4 houses (which
return to the bank) plus the price on the title deed. Only 1 hotel may be built per
street.

## Selling Land Between Players

Unimproved land, stations, and utilities may be sold between players (bypassing the
bank) at any mutually agreed price. Built-up streets may not be traded — as long as
even a single house stands anywhere in a colour group, nothing from that group may be
sold until the owner first sells all houses in it back to the bank.

Houses and hotels may never be sold to other players — only back to the bank, at half
their printed price. For hotels, the bank pays half the hotel's price plus half the
price of the four houses it received when the hotel was built. All hotels in one
colour group may be sold at once. If needed, a hotel may instead be exchanged back for
4 houses plus cash (half the hotel's price), freeing up cash without selling outright.

Mortgaged property may only be sold to other players, never back to the bank.

## Mortgaging

If you're out of cash and owe a debt, you may raise money by mortgaging one or more
of your properties to the bank — first sell any houses/hotels on it. Turn the title
deed face down; the bank pays you the mortgage value printed on the back of the card.

- To lift a mortgage, pay the borrowed amount plus **10% interest** to the bank.
- Mortgaged land remains the owner's property; other players cannot take it over by
  paying off the mortgage themselves.
- No rent may be collected for mortgaged land, but rent may still be collected on
  other, unmortgaged streets of the same colour group.
- Mortgaged land may be sold to another player at an agreed price. The buyer may then
  either immediately pay off the mortgage plus 10% interest, or just pay the 10%
  interest and keep the mortgage in place (in which case, when it is later lifted,
  the owner again pays the mortgage value plus 10% interest).
- No houses may be bought in a colour group until every mortgage in that group has
  been lifted.

## Bankruptcy

If you owe the bank or another player more than you can pay, you are declared
bankrupt and are out of the game.

- **Debt owed to the bank**: all your money and property return to the bank, which
  auctions the land off one piece at a time to the highest bidder. Any "Get Out of
  Jail Free" cards go to the bottom of their deck.
- **Debt owed to another player**: your houses and hotels are bought by the bank at
  half price first. Your creditor then receives all your money, title deeds, and
  "Get Out of Jail Free" cards, as well as any mortgaged property you held — for
  which the creditor must immediately pay 10% interest and choose whether to keep
  the mortgage or pay it off.

## General Remarks

- If a player owes more rent than they have in cash, they may (partially) settle the
  debt with property (i.e. unimproved land). The creditor may accept a specific
  street — even a mortgaged one — regardless of whether the debt is larger or smaller
  than the street's real value, e.g. to gain a new building opportunity or to prevent
  another player from completing a colour group.
- Owners of streets and other property must always claim their own rent — it is not
  collected automatically.
- The bank only lends players money against collateral, i.e. by mortgaging property.
  Players may not lend each other money.

## Winner

The last player left in the game wins.

## Short Game Variant

This variant differs from the standard rules in three ways:

1. **Setup**: during setup, the banker shuffles all title deeds. The player to the
   banker's left cuts the stack, and the bank deals two cards to every player (one at
   a time). Players immediately pay the bank the official price of the properties
   they received. Play then begins.
2. In this short game, only **3 houses** (instead of 4) are built per street before a
   hotel may be bought. Hotel rent is unchanged from the standard game. Selling a
   hotel returns half its price plus 3 houses.
3. **End of game**: the first player to go bankrupt withdraws as usual. As soon as a
   *second* player goes bankrupt, the game ends immediately. That bankrupt player
   hands their creditor everything of value they own, including houses, hotels, and
   other property. The remaining players then total the value of their holdings:
   1. cash on hand;
   2. the board-printed price of streets, utilities, or stations they own;
   3. half the board-printed price of any mortgaged property they hold;
   4. the purchase price of houses they own;
   5. the purchase price of hotels they own (= the price of four houses).

   The richest player wins the game.

## Timed Game Variant

Another way to shorten the game: agree on a fixed playing time before starting. The
richest player when time runs out wins. Before play begins, shuffle and cut the title
deeds and deal two cards to each player, who immediately pays the bank the purchase
price of the properties received. Play then begins.
