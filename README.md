# World Generation Game (CS61B)

## Game Design

In this game, players need to give a seed to initialize a world. The world is randomly generated with the seed and the world should be the same if given the same seed. In the world, there are some rooms and some hallways connecting the rooms. There are also lightings and doors in the rooms or the hallways. Player is trapped in the dungeon and he needs to escape from it. At first, it was dark, and player can only see the environment around him. By turning on the lights, the tiles around the lights will be visible. Players need to turn on all the lights to light up the world and find the key to the exit lock door and get out of the room.  Only when all the lights are turned on and the key is found can player escaped. The time is limited, player only have 5 minutes to escape.

## Algorithms

In this project, A* algorithm is applied for the world generation. We can use the A* algorithm to find the closest path. After finding the closest path, we connect rooms one by one. This makes sure all rooms are successfully connected.

## Functions implemented

- [X] Control and movement of the characters as well as interaction with the scene components like doors.
- [x] Saving and loading of the game.
- [x] The font end of the game including the start menu and the in-game menu.
- [x] The partial rendering of the game, which means gamers can only part of the scene around the player is visible.
- [x] Game design and the judgement of win or lose.



