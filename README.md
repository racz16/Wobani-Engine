# Engine
My OpenGL game engine. I write the code in Java, using the LWJGL.

## Features
- GameObject based scene graph
- Component system, including dependency handling
- orthographic and perspective camera with frustum culling (sphere and AABB)
- load various model and texture formats
- Bezier and Catmull-Rom splines
- MSAA, vSync, various texture filters, wireframe mode
- customizable rendering pipeline and material system
- Blinn-Phong shading
  - directional, point and spotlights
  - normal mapping and POM
  - grid based lighting
- shadow mapping with PCF (only for directional light)
- post processing effects like tone mapping or FXAA
- parallax corrected reflections and refractions
- data caching mechanisms
- keyboard, mouse and joystick input handling
- logging and statistics

Schedule for new features: https://trello.com/b/Kt2S5Tz4/engine

## Known issues
- wrong shadows on surfaces using POM
- POM distortion using orthographic camera
- works only on Windows

## Build
The source code contains the IntelliJ project files, so IntelliJ can directly open it as a standard Java project. The code contains Java 8 features, so you need JDK 8+ installed in your computer and the following LWJGL components:
- LWJGL core 3.2.0
- JOML 1.9.10
- Assimp bindings
- GLFW bindings
- OpenGL bindings
- OpenAL bindings
- stb bindings

You can download LWJGL components from here: https://www.lwjgl.org/customize (minimal OpenGL preset can be a good start)

## Run
You need an OpenGL 4.5+ compatible GPU and JRE 8+ installed in your computer. I also recommend you to update your drivers. For example my integrated GPU's initial driver did not handle all OpenGL 4.5 features well, but after a driver update it works well.