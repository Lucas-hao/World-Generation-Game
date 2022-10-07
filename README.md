# World Generation Game (CS61B)

## Game Design

In this game, players need to input a seed to generate a random map. Different seeds will produce different maps. There are doors, corridors, lights and rooms. Players can control and move the character around. They are allowed to interact with the doors and lights as well. To win this game, players need to open all the doors in the rooms. Otherwise, players will lose if they quit the game without opening all the doors. Players can also toggle the partial rendering mode, where only positions around the characters are visible.

## Algorithms

In this project, A* algorithm is applied for the world generation. We can use the A* algorithm to find the closest path
After finding the closest path, we connect rooms one by one. This makes sure all rooms are successfully connected.

## Functions implemented

- [X] Control and movement of the characters as well as interaction with the scene components like doors.
- [x] Saving and loading of the game.
- [x] The font end of the game including the start menu and the in-game menu.
- [x] The partial rendering of the game, which means gamers can only part of the scene around the player is visible.



