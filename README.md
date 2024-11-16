###### Copyright 2024 Farcasanu Bianca-Ioana 323CA

# GwentStone
GwenStone is a strategic, turn-based card game where players choose a deck,
summon minions and utilize powerful heroes to beat their opponents.
Players must carefully manage resources, deploy cards and attack strategically
to achieve victory.

## Structure
The project structure is organised into 5 packages, each serving a specific
purpose in the implementation of the game:
1. **cards Package** contains:
    - class `Card`: The base class representing a generic card. It provides
   common properties like name, mana cost, frozen status, coordinates etc.
   It also has an attribute *cardInfo* of type CardInput which contains all details given
   in input.
    - class `Hero`: Extends *Card* and adds unique abilities specific to each hero.
    - class `Minion`: Also extends *Card*, representing creatures summoned to the
   game table.
2. **gameprocess Package** handles the gameplay mechanics and player interactions:
    - class `Table`: Represents the 4x5 game table where cards are placed. It
   manages the rows and columns associated with each player.
    - class `Player`: Handles a player's hero, deck, hand, mana, and in-game actions.
    - class `Game`: Extends *Table* and manages the main gameplay flow, making sure
   the rules are followed and handling player turns.
3. **inputloader Package** is responsible for initializing and processing game actions:
    - class `InitGame`: Acts as the engine of the game. It initializes the game state
   and processes commands using instances of *ActionCommands* and *DebugCommands* classes.
    - class `ActionCommands`: Processes all the action commands received in input.
    - class `DebugCommands`: Processes all the debug commands received in input.
4. **fileio Package** contains classes in which the input is stored.
5. **main Package** contains the classes Main and Test that run the program.

## Features
- Heroes with Unique Abilities: Heroes provide game-changing powers, but require
strategic use of mana and timing.
- Dynamic Minion Cards: Summon and manage a variety of minion cards, each with
specific abilities and stats like health, attack, and special effects.
- Interactive Game Board: Cards are placed on a 4x5 game table with rows divided
into front and back for each player. Strategic placement is key.
- Mana Management: Players gain mana each round, with a cap, requiring careful
allocation to play cards or use hero abilities.
- Win Conditions: Defeat your opponent’s hero to claim victory.

## Classes and Concepts

The whole game can be composed of a series of games as the players can
restart the game if they want to, but they would be assigned a new hero, a
different starting player, and also they can choose another deck from their
own list of decks provided at the beginning of the whole game.
After choosing a deck, each player can get cards in their hand and place
them on table afterward.
The game table is the main place where action takes place. It is a 4x5 grid
where minions are placed on specific rows, depending on the ability of each
card. Strategic positioning affects gameplay.

There are different types of cards:
### Hero
1. Represents the player's main character.
2. Heroes have health, mana costs for abilities, and unique effects:
* Lord Royce: Freezes enemy minions on a targeted row.
* Empress Thorina: Destroys the strongest enemy minion.
* King Mudface: Heals allied minions on a targeted row.
* General Kocioraw: Buffs the attack of allied minions on a targeted row.

### Minion Cards
1. Represent creatures summoned to the board.
2. Each minion has attributes like health, attackDamage, abilities (freezing,
healing or swapping stats).

## Key Methods
1. *placeCardFromHandOnTable*
    - Places a card from the player’s hand onto the game table, ensuring
   mana and row constraints are met.
2. *cardUsesAbility*
    - Executes the special ability of a minion, affecting allied or enemy cards.
3. *attackHero*
    - Attacks the opponent's hero, checking for any tank cards protecting them.
4. *useHeroAbility*
    - Activates the player's hero ability on a specific row, with effects
   depending on the hero type.
5. *startNewRound*
    - Sets up the environment for a new round
    - Each player gets a new card from their deck
    - Marks that none of the players has attacked yet
    - Marks all the cards on the table (minions and heroes included) that they
   have not been used for attack so far
