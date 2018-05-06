# Wobani Engine

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/8d2dcf1afc754bcdb5576bebc5493988)](https://app.codacy.com/app/racz1666/Wobani-Engine?utm_source=github.com&utm_medium=referral&utm_content=racz16/Wobani-Engine&utm_campaign=badger)

My own OpenGL game engine. I write the code in Java, using the LWJGL.

## Version 0.2 main goals
- PBR rendering
- terrain system
- UI
- level editor
- loading/saving

## Features
- GameObject based scene graph
- Component system, including dependency handling
- chance to store Components based on type
- orthographic and perspective camera with frustum culling (sphere and AABB)
- load various model and texture formats
- Bezier and Catmull-Rom splines
- MSAA, vSync, various texture filterings, wireframe mode
- customizable rendering pipeline and material system
- Blinn-Phong shading
  - directional, point and spotlights
  - normal mapping and POM
- shadow mapping with PCF (only for directional light)
- post processing effects like tone mapping or FXAA
- data caching mechanisms
- keyboard, mouse and joystick input handling
- logging and statistics

Schedule for new features: https://trello.com/b/Kt2S5Tz4/engine

## Known issues
- wrong shadows on surfaces using POM
- POM distortion using orthographic camera
- works only on Windows

## How to build
The source code contains the Netbeans project files, so Netbeans can directly open it as a standard Java project. The following LWJGL components need to build the program:
- LWJGL core 3.1.2
- JOML 1.9.4
- Assimp bindings
- GLFW bindings
- OpenGL bindings
- stb bindings

You can download LWJGL components from here: https://www.lwjgl.org/customize
