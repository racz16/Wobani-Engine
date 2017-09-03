package examples;

import core.*;
import javax.swing.*;
import org.joml.*;
import renderers.Renderer;
import renderers.*;
import renderers.postProcessing.*;
import resources.*;
import resources.textures.EasyFiltering.TextureFiltering;
import toolbox.*;
import toolbox.annotations.*;
import window.*;

/**
 * Example window which demonstrates the Engine's features.
 */
public class Example1Window extends javax.swing.JFrame {

    /**
     * Indicates that the an OpenGL related setting is changed.
     */
    private boolean openGLRelatedSettingsChanged;
    /**
     * Indicates that the texture filtering setting is changed.
     */
    private boolean textureFilteringChanged;
    /**
     * Indicates that the vSync setting is changed.
     */
    private boolean vSyncChanged;
    /**
     * Indicates that the gamma correction setting is changed.
     */
    private boolean gammaChanged;
    /**
     * Indicates that the fullscreen setting is changed.
     */
    private boolean fullscreen;

    /**
     * Initializes a new ExampleWindow.
     */
    public Example1Window() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Utility.logException(ex);
        }

        initComponents();
        spGammaValue.setModel(new SpinnerNumberModel(new Float(2.2f), new Float(1.99f), new Float(2.5f), new Float(0.1f)));

        spWidth.setValue(Window.getClientAreaSize().x);
        spHeight.setValue(Window.getClientAreaSize().y);
        cbFullscreen.setSelected(Window.isFullscreen());
        cbShadows.setSelected(Settings.isShadowMapping());
        setSelectedItem(Settings.getShadowMapResolution(), cbShadowMapSize);
        setSelectedItem(Settings.getMsaaLevel(), cbMsaa);
        cbTextureFiltering.setSelectedIndex(Settings.getTextureFiltering().getIndex());
        cbVSync.setSelectedIndex(Window.getVSync());
    }

    /**
     * Selects the given value in the given ComboBox.
     *
     * @param value value
     * @param cb ComboBox
     */
    private void setSelectedItem(int value, @NotNull JComboBox<String> cb) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (Integer.valueOf(cb.getItemAt(i)) == value) {
                cb.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Updates the settings.
     */
    public void update() {
        if (openGLRelatedSettingsChanged) {
            changeOpenGLRelatedSettings();
            openGLRelatedSettingsChanged = false;
        }
    }

    /**
     * Updates the statistics on the UI.
     */
    public void updateStats() {
        updateTextureData();
        updateMeshData();
        updateSplineData();
        updateFboData();
        updateVaoData();
        updateUboData();
        updateShaderData();
        updateImportant();
    }

    /**
     * Updates the texture statistics on the UI.
     */
    private void updateTextureData() {
        Vector3i data = ResourceManager.getTextureData();
        lblTextureNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblTextureSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblTextureSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    /**
     * Updates the mesh statistics on the UI.
     */
    private void updateMeshData() {
        Vector3i data = ResourceManager.getMeshData();
        lblMeshNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblMeshSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblMeshSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    /**
     * Updates the spline statistics on the UI.
     */
    private void updateSplineData() {
        Vector3i data = ResourceManager.getSplineData();
        lblSplineNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblSplineSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblSplineSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    /**
     * Updates the FBO statistics on the UI.
     */
    private void updateFboData() {
        lblFboNumber.setText(ResourceManager.getFboData().x + "");
    }

    /**
     * Updates the VAO statistics on the UI.
     */
    private void updateVaoData() {
        lblVaoNumber.setText(ResourceManager.getVaoData().x + "");
    }

    /**
     * Updates the UBO statistics on the UI.
     */
    private void updateUboData() {
        Vector3i data = ResourceManager.getUboData();
        lblUboNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblUboSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblUboSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    /**
     * Updates the shader statistics on the UI.
     */
    private void updateShaderData() {
        lblShaderNumber.setText(ResourceManager.getShaderData().x + "");
    }

    /**
     * Updates the fps, frame rendering time, rendered triangles and rendering
     * elements on the UI.
     */
    private void updateImportant() {
        lblFps.setText(Time.getFps() + " FPS");
        float milisecs = 1.0f / Time.getFps();
        lblMsec.setText(milisecs + " msec");

        int triangles = 0;
        int meshes = 0;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(true); i++) {
            Renderer renderer = RenderingPipeline.getRenderer(true, i);
            if (renderer.isActive()) {
                triangles += renderer.getNumberOfRenderedFaces();
                meshes += renderer.getNumberOfRenderedElements();
            }
        }
        lblRenderedElements.setText(meshes + " meshes/splines");
        lblRenderedTriangles.setText(triangles + " triangles");
    }

    /**
     * Sets the OpenGL related settings changes.
     */
    private void changeOpenGLRelatedSettings() {
        if (textureFilteringChanged) {
            switch (cbTextureFiltering.getSelectedIndex()) {
                case 0:
                    Settings.setTextureFiltering(TextureFiltering.NONE);
                    break;
                case 1:
                    Settings.setTextureFiltering(TextureFiltering.BILINEAR);
                    break;
                case 2:
                    Settings.setTextureFiltering(TextureFiltering.TRILINEAR);
                    break;
                case 3:
                    Settings.setTextureFiltering(TextureFiltering.ANISOTROPIC_2X);
                    break;
                case 4:
                    Settings.setTextureFiltering(TextureFiltering.ANISOTROPIC_4X);
                    break;
                case 5:
                    Settings.setTextureFiltering(TextureFiltering.ANISOTROPIC_8X);
                    break;
                case 6:
                    Settings.setTextureFiltering(TextureFiltering.ANISOTROPIC_16X);
                    break;
                default:
                    Utility.log("Undefined texture filtering");
            }
            textureFilteringChanged = false;
        }
        if (vSyncChanged) {
            Window.setVSync(cbVSync.getSelectedIndex());
            vSyncChanged = false;
        }
        if (gammaChanged) {
            if (cbGammaCorrection.getSelectedIndex() == 0) {
                Settings.setGamma(1);
            } else {
                Settings.setGamma((float) spGammaValue.getValue());
            }
            gammaChanged = false;
        }
        if (fullscreen) {
            Window.setFullscreen(cbFullscreen.isSelected());
            fullscreen = false;
        }
    }

    /**
     * Updates the GLFW window's dimensions on the settings UI.
     */
    public void updateSettingsWindowDimensions() {
        Vector2i size = Window.getSize();
        spWidth.setValue(size.x);
        spHeight.setValue(size.y);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        lblFps = new javax.swing.JLabel();
        lblRenderedElements = new javax.swing.JLabel();
        lblRenderedTriangles = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblTextureNumber = new javax.swing.JLabel();
        lblTextureSize = new javax.swing.JLabel();
        lblTextureSizeMega = new javax.swing.JLabel();
        lblMeshNumber = new javax.swing.JLabel();
        lblMeshSize = new javax.swing.JLabel();
        lblMeshSizeMega = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        lblSplineNumber = new javax.swing.JLabel();
        lblSplineSize = new javax.swing.JLabel();
        lblSplineSizeMega = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        lblFboNumber = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lblVaoNumber = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        lblUboNumber = new javax.swing.JLabel();
        lblUboSize = new javax.swing.JLabel();
        lblUboSizeMega = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        lblShaderNumber = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        lblTextureSize1 = new javax.swing.JLabel();
        lblTextureSizeMega1 = new javax.swing.JLabel();
        lblMsec = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        cbWireframe = new javax.swing.JCheckBox();
        cbFrustumCulling = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        spWidth = new javax.swing.JSpinner();
        spHeight = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cbFullscreen = new javax.swing.JCheckBox();
        cbShadows = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        cbMsaa = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        cbTextureFiltering = new javax.swing.JComboBox<>();
        cbVSync = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        cbShadowMapSize = new javax.swing.JComboBox<>();
        cbGammaCorrection = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        spGammaValue = new javax.swing.JSpinner();
        cbToneMapping = new javax.swing.JCheckBox();
        cbInvert = new javax.swing.JCheckBox();
        cbGrayscale = new javax.swing.JCheckBox();
        cbFxaa = new javax.swing.JCheckBox();

        setTitle("Example");
        setResizable(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                moved(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exit(evt);
            }
        });

        lblFps.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblFps.setText(" ");
        lblFps.setToolTipText("");

        lblRenderedElements.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblRenderedElements.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRenderedElements.setText(" ");
        lblRenderedElements.setToolTipText("");

        lblRenderedTriangles.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblRenderedTriangles.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblRenderedTriangles.setText(" ");
        lblRenderedTriangles.setToolTipText("");

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel1.setText("Textures");

        lblTextureNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblTextureNumber.setText(" ");

        lblTextureSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        lblTextureSizeMega.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblTextureSizeMega.setText(" ");

        lblMeshNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblMeshNumber.setText(" ");

        lblMeshSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        lblMeshSizeMega.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblMeshSizeMega.setText(" ");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel2.setText("Meshes");

        lblSplineNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblSplineNumber.setText(" ");

        lblSplineSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        lblSplineSizeMega.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblSplineSizeMega.setText(" ");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel8.setText("Splines");

        lblFboNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblFboNumber.setText(" ");

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel12.setText("FBOs");

        lblVaoNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblVaoNumber.setText(" ");

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel13.setText("VAOs");

        lblUboNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblUboNumber.setText(" ");

        lblUboSize.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        lblUboSizeMega.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblUboSizeMega.setText(" ");

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel14.setText("UBOs");

        lblShaderNumber.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        jLabel15.setText("Shaders");

        lblTextureSize1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblTextureSize1.setText("IN VRAM");

        lblTextureSizeMega1.setFont(new java.awt.Font("Tahoma", 0, 14)); // NOI18N
        lblTextureSizeMega1.setText("IN RAM");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel8)
                    .addComponent(jLabel12)
                    .addComponent(jLabel14)
                    .addComponent(jLabel13)
                    .addComponent(jLabel15))
                .addGap(39, 39, 39)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(lblShaderNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblUboNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                            .addComponent(lblVaoNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblFboNumber, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblSplineNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblSplineSize, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblUboSize, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 253, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                                .addGap(68, 68, 68)
                                .addComponent(lblTextureSize1, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblTextureNumber, javax.swing.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                                    .addComponent(lblMeshNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lblTextureSize, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)
                                    .addComponent(lblMeshSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMeshSizeMega, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTextureSizeMega, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblTextureSizeMega1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblSplineSizeMega, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblUboSizeMega, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTextureSize1)
                    .addComponent(lblTextureSizeMega1))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel12)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel13)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel15))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblTextureNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTextureSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblTextureSizeMega))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblMeshNumber, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMeshSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(lblMeshSizeMega))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(lblSplineSize, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(lblSplineSizeMega)
                                    .addGap(13, 13, 13))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(lblSplineNumber)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))))
                        .addComponent(lblFboNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblVaoNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(lblUboSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(lblUboSizeMega))
                            .addComponent(lblUboNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblShaderNumber, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblMsec.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblMsec.setText(" ");
        lblMsec.setToolTipText("");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblFps, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMsec, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(lblRenderedElements, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(lblRenderedTriangles, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFps, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblRenderedTriangles)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRenderedElements)
                    .addComponent(lblMsec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67))
        );

        jTabbedPane1.addTab("Statistics", jPanel1);

        cbWireframe.setText("Wireframe mode");
        cbWireframe.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setWireframeMode(evt);
            }
        });

        cbFrustumCulling.setSelected(true);
        cbFrustumCulling.setText("Frustum culling");
        cbFrustumCulling.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setFrustumCulling(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel3.setText("Width");

        spWidth.setModel(new javax.swing.SpinnerNumberModel(640, 200, null, 1));
        spWidth.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                setWidth(evt);
            }
        });

        spHeight.setModel(new javax.swing.SpinnerNumberModel(360, 100, null, 1));
        spHeight.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                setHeight(evt);
            }
        });

        jLabel4.setText("Height");

        jLabel5.setText("Shadowmap size");

        cbFullscreen.setText("Fullscreen");
        cbFullscreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setFullscreen(evt);
            }
        });

        cbShadows.setSelected(true);
        cbShadows.setText("Shadows");
        cbShadows.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setShadows(evt);
            }
        });

        jLabel6.setText("MSAA");

        cbMsaa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "4", "8", "16" }));
        cbMsaa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setMsaa(evt);
            }
        });

        jLabel7.setText("Texture filtering");

        cbTextureFiltering.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Bilinear", "Trilinear", "Anisotropic 2x", "Anisotropic 4x", "Anisotropic 8x", "Anisotropic 16x" }));
        cbTextureFiltering.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setTextureFiltering(evt);
            }
        });

        cbVSync.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off", "60 fps", "30 fps" }));
        cbVSync.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setVSync(evt);
            }
        });

        jLabel9.setText("VSync");

        cbShadowMapSize.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "128", "256", "512", "1024", "2048", "4096" }));
        cbShadowMapSize.setSelectedIndex(4);
        cbShadowMapSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setShadowMapSize(evt);
            }
        });

        cbGammaCorrection.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off", "On" }));
        cbGammaCorrection.setSelectedIndex(1);
        cbGammaCorrection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setGammaCorrection(evt);
            }
        });

        jLabel10.setText("Gamma correction");

        jLabel11.setText("Value");

        spGammaValue.setModel(new javax.swing.SpinnerNumberModel(2.2f, null, null, 1.0f));
        spGammaValue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                setGammaValue(evt);
            }
        });

        cbToneMapping.setText("Tone mapping");
        cbToneMapping.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setToneMapping(evt);
            }
        });

        cbInvert.setText("Invert");
        cbInvert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setInvert(evt);
            }
        });

        cbGrayscale.setText("Grayscale");
        cbGrayscale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setGrayscale(evt);
            }
        });

        cbFxaa.setText("FXAA");
        cbFxaa.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setFxaa(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbFrustumCulling)
                    .addComponent(cbWireframe)
                    .addComponent(cbInvert)
                    .addComponent(cbGrayscale)
                    .addComponent(cbToneMapping, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbFxaa))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbMsaa, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbVSync, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbTextureFiltering, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbShadows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(spWidth, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbGammaCorrection, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(spHeight)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(cbShadowMapSize, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cbFullscreen, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(spGammaValue, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(44, 44, 44))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(spWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(spHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbFullscreen))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbShadows)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cbShadowMapSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbMsaa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbTextureFiltering, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbVSync, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbGammaCorrection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spGammaValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cbWireframe)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbFrustumCulling)
                        .addGap(18, 18, 18)
                        .addComponent(cbInvert)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbGrayscale)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbFxaa)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cbToneMapping)))
                .addGap(43, 43, 43))
        );

        jTabbedPane1.addTab("Settings", jPanel2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 673, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Handles window move changes.
     *
     * @param evt event
     */
    private void moved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_moved
        if (!Window.isFullscreen()) {
            Example1.setWindowPositions(false);
        }
    }//GEN-LAST:event_moved

    /**
     * Handles window width change.
     *
     * @param evt event
     */
    private void setWidth(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_setWidth
        Window.setClientAreaSize(new Vector2i((int) spWidth.getValue(), Window.getClientAreaSize().y));
    }//GEN-LAST:event_setWidth

    /**
     * Handles window height change.
     *
     * @param evt event
     */
    private void setHeight(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_setHeight
        Window.setClientAreaSize(new Vector2i(Window.getClientAreaSize().x, (int) spHeight.getValue()));
    }//GEN-LAST:event_setHeight

    /**
     * Handles shadow map size change.
     *
     * @param evt event
     */
    private void setShadowMapSize(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setShadowMapSize
        Settings.setShadowMapResolution(Integer.valueOf(cbShadowMapSize.getItemAt(cbShadowMapSize.getSelectedIndex())));
    }//GEN-LAST:event_setShadowMapSize

    /**
     * Handles vSync change.
     *
     * @param evt event
     */
    private void setVSync(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setVSync
        vSyncChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_setVSync

    /**
     * Terminates the program.
     *
     * @param evt event
     */
    private void exit(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exit
        Window.setWindowShouldClose(true);
    }//GEN-LAST:event_exit

    /**
     * Sets whether or not the wireframe mode should be enabled.
     *
     * @param evt event
     */
    private void setWireframeMode(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setWireframeMode
        Settings.setWireframeMode(cbWireframe.isSelected());
    }//GEN-LAST:event_setWireframeMode

    /**
     * Sets whether or not the frustum culling should be enabled.
     *
     * @param evt event
     */
    private void setFrustumCulling(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setFrustumCulling
        Settings.setFrustumCulling(cbFrustumCulling.isSelected());
    }//GEN-LAST:event_setFrustumCulling

    /**
     * Sets whether or not the shadows should be enabled.
     *
     * @param evt event
     */
    private void setShadows(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setShadows
        Settings.setShadowMapping(cbShadows.isSelected());
    }//GEN-LAST:event_setShadows

    /**
     * Handles texture filtering change.
     *
     * @param evt event
     */
    private void setTextureFiltering(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setTextureFiltering
        textureFilteringChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_setTextureFiltering

    /**
     * Handles gamma correction value change.
     *
     * @param evt event
     */
    private void setGammaValue(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_setGammaValue
        gammaChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_setGammaValue

    /**
     * Sets whether or not the tone mapping post processing should be enabled.
     *
     * @param evt event
     */
    private void setToneMapping(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setToneMapping
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == ReinhardToneMappingRenderer.class) {
                renderer.setActive(cbToneMapping.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_setToneMapping

    /**
     * Handles MSAA change.
     *
     * @param evt event
     */
    private void setMsaa(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setMsaa
        if (cbMsaa.getSelectedIndex() == 0) {
            Settings.setMsaaLevel(1);
            return;
        }
        Settings.setMsaaLevel(2 << cbMsaa.getSelectedIndex() - 1);
    }//GEN-LAST:event_setMsaa

    /**
     * Sets whether or not the gamma correction should be enabled.
     *
     * @param evt event
     */
    private void setGammaCorrection(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setGammaCorrection
        gammaChanged = true;
        openGLRelatedSettingsChanged = true;
        spGammaValue.setEnabled(cbGammaCorrection.getSelectedIndex() == 1);
    }//GEN-LAST:event_setGammaCorrection

    /**
     * Sets whether or not the invert post processing should be enabled.
     *
     * @param evt event
     */
    private void setInvert(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setInvert
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == InvertRenderer.class) {
                renderer.setActive(cbInvert.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_setInvert

    /**
     * Sets whether or not the grayscale post processing should be enabled.
     *
     * @param evt event
     */
    private void setGrayscale(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setGrayscale
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == GrayscaleRenderer.class) {
                renderer.setActive(cbGrayscale.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_setGrayscale

    /**
     * Sets whether or not the FXAA post processing should be enabled.
     *
     * @param evt event
     */
    private void setFxaa(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setFxaa
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == FxaaRenderer.class) {
                renderer.setActive(cbFxaa.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_setFxaa

    /**
     * Sets whether or not the fullscreen should be enabled.
     *
     * @param evt event
     */
    private void setFullscreen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setFullscreen
        fullscreen = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_setFullscreen

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cbFrustumCulling;
    private javax.swing.JCheckBox cbFullscreen;
    private javax.swing.JCheckBox cbFxaa;
    private javax.swing.JComboBox<String> cbGammaCorrection;
    private javax.swing.JCheckBox cbGrayscale;
    private javax.swing.JCheckBox cbInvert;
    private javax.swing.JComboBox<String> cbMsaa;
    private javax.swing.JComboBox<String> cbShadowMapSize;
    private javax.swing.JCheckBox cbShadows;
    private javax.swing.JComboBox<String> cbTextureFiltering;
    private javax.swing.JCheckBox cbToneMapping;
    private javax.swing.JComboBox<String> cbVSync;
    private javax.swing.JCheckBox cbWireframe;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JLabel lblFboNumber;
    private javax.swing.JLabel lblFps;
    private javax.swing.JLabel lblMeshNumber;
    private javax.swing.JLabel lblMeshSize;
    private javax.swing.JLabel lblMeshSizeMega;
    private javax.swing.JLabel lblMsec;
    private javax.swing.JLabel lblRenderedElements;
    private javax.swing.JLabel lblRenderedTriangles;
    private javax.swing.JLabel lblShaderNumber;
    private javax.swing.JLabel lblSplineNumber;
    private javax.swing.JLabel lblSplineSize;
    private javax.swing.JLabel lblSplineSizeMega;
    private javax.swing.JLabel lblTextureNumber;
    private javax.swing.JLabel lblTextureSize;
    private javax.swing.JLabel lblTextureSize1;
    private javax.swing.JLabel lblTextureSizeMega;
    private javax.swing.JLabel lblTextureSizeMega1;
    private javax.swing.JLabel lblUboNumber;
    private javax.swing.JLabel lblUboSize;
    private javax.swing.JLabel lblUboSizeMega;
    private javax.swing.JLabel lblVaoNumber;
    private javax.swing.JSpinner spGammaValue;
    private javax.swing.JSpinner spHeight;
    private javax.swing.JSpinner spWidth;
    // End of variables declaration//GEN-END:variables

}
