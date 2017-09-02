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
import window.*;

public class Example1Window extends javax.swing.JFrame {

    private boolean openGLRelatedSettingsChanged;
    private boolean dimensionChanged;
    private boolean textureFilteringChanged;
    private boolean vSyncChanged;
    private boolean gammaChanged;
    private boolean fullscreen;

    public Example1Window() {
        //GUI windows stílusú
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Utility.logException(ex);
        }

        initComponents();
        gammaValue.setModel(new SpinnerNumberModel(new Float(2.2f), new Float(1.99f), new Float(2.5f), new Float(0.1f)));

        width.setValue(Window.getClientAreaSize().x);
        height.setValue(Window.getClientAreaSize().y);
        cFullscreen.setSelected(Window.isFullscreen());
        cShadows.setSelected(Settings.isShadowMapping());
        setSelectedItem(Settings.getShadowMapResolution(), shadowMapSize);
        setSelectedItem(Settings.getMsaaLevel(), msaa);
        textureFiltering.setSelectedIndex(Settings.getTextureFiltering().getIndex());
        vSync.setSelectedIndex(Window.getVSync());
    }

    private void setSelectedItem(int value, JComboBox<String> cb) {
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (Integer.valueOf(cb.getItemAt(i)) == value) {
                cb.setSelectedIndex(i);
                break;
            }
        }
    }

    public void update() {
        if (openGLRelatedSettingsChanged) {
            changeOpenGLRelatedSettings();
            openGLRelatedSettingsChanged = false;
        }

    }

    public void updateStats() {
        getTextureData();
        getMeshData();
        getSplineData();
        getFboData();
        getVaoData();
        getUboData();
        getShaderData();
        updateImportant();
    }

    private void getTextureData() {
        Vector3i data = ResourceManager.getTextureData();
        lblTextureNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblTextureSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblTextureSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    private void getMeshData() {
        Vector3i data = ResourceManager.getMeshData();
        lblMeshNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblMeshSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblMeshSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    private void getSplineData() {
        Vector3i data = ResourceManager.getSplineData();
        lblSplineNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblSplineSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblSplineSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    private void getFboData() {
        lblFboNumber.setText(ResourceManager.getFboData().x + "");
    }

    private void getVaoData() {
        lblVaoNumber.setText(ResourceManager.getVaoData().x + "");
    }

    private void getUboData() {
        Vector3i data = ResourceManager.getUboData();
        lblUboNumber.setText(data.x + "");
        float size = data.z;
        float sizeM = size / (1024 * 1024);
        lblUboSize.setText(size + " B  (" + sizeM + " MB)");
        size = data.y;
        sizeM = size / (1024 * 1024);
        lblUboSizeMega.setText(size + " B  (" + sizeM + " MB)");
    }

    private void getShaderData() {
        lblShaderNumber.setText(ResourceManager.getShaderData().x + "");
    }

    private void changeOpenGLRelatedSettings() {
        if (dimensionChanged) {
            if (Window.getClientAreaSize().x != (int) width.getValue()) {
                Window.setClientAreaSize(new Vector2i((int) width.getValue(), Window.getClientAreaSize().y));
            } else {
                Window.setClientAreaSize(new Vector2i(Window.getClientAreaSize().x, (int) height.getValue()));
            }
            dimensionChanged = false;
        }
        if (textureFilteringChanged) {
            switch (textureFiltering.getSelectedIndex()) {
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
            Window.setVSync(vSync.getSelectedIndex());
            vSyncChanged = false;
        }
        if (gammaChanged) {
            if (gammaLevel.getSelectedIndex() == 0) {
                Settings.setGamma(1);
            } else {
                Settings.setGamma((float) gammaValue.getValue());
            }
            gammaChanged = false;
        }
        if (fullscreen) {
            Window.setFullscreen(cFullscreen.isSelected());
            fullscreen = false;
        }
    }

    private void updateImportant() {
        lblFPS.setText(Time.getFps() + " FPS");
        float milisecs = 1.0f / Time.getFps();
        lblMilisec.setText(milisecs + " msec");

        int triangles = 0;
        int meshes = 0;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(true); i++) {
            Renderer renderer = RenderingPipeline.getRenderer(true, i);
            if (renderer.isActive()) {
                triangles += renderer.getNumberOfRenderedFaces();
                meshes += renderer.getNumberOfRenderedElements();
            }
        }
        renderedModels.setText(meshes + " meshes/splines");
        renderedTriangles.setText(triangles + " triangles");
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        lblFPS = new javax.swing.JLabel();
        renderedModels = new javax.swing.JLabel();
        renderedTriangles = new javax.swing.JLabel();
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
        lblMilisec = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        cWireframeMode = new javax.swing.JCheckBox();
        cFrustumCulling = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        jLabel3 = new javax.swing.JLabel();
        width = new javax.swing.JSpinner();
        height = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        cFullscreen = new javax.swing.JCheckBox();
        cShadows = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        msaa = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        textureFiltering = new javax.swing.JComboBox<>();
        vSync = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        shadowMapSize = new javax.swing.JComboBox<>();
        gammaLevel = new javax.swing.JComboBox<>();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        gammaValue = new javax.swing.JSpinner();
        hdr = new javax.swing.JCheckBox();
        invert = new javax.swing.JCheckBox();
        grayscale = new javax.swing.JCheckBox();
        fxaa = new javax.swing.JCheckBox();

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

        lblFPS.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblFPS.setText(" ");
        lblFPS.setToolTipText("Frame per sec");

        renderedModels.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        renderedModels.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        renderedModels.setText(" ");
        renderedModels.setToolTipText("Rendered models");

        renderedTriangles.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        renderedTriangles.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        renderedTriangles.setText(" ");
        renderedTriangles.setToolTipText("Rendered triangles");

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

        lblMilisec.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lblMilisec.setText(" ");
        lblMilisec.setToolTipText("Frame per sec");

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
                            .addComponent(lblFPS, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lblMilisec, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(renderedModels, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
                            .addComponent(renderedTriangles, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblFPS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(renderedTriangles)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(renderedModels)
                    .addComponent(lblMilisec, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(67, 67, 67))
        );

        jTabbedPane1.addTab("Statistics", jPanel1);

        cWireframeMode.setText("Wireframe mode");
        cWireframeMode.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setWireframeMode(evt);
            }
        });

        cFrustumCulling.setSelected(true);
        cFrustumCulling.setText("Frustum culling");
        cFrustumCulling.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setFrustumCulling(evt);
            }
        });

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jLabel3.setText("Width");

        width.setModel(new javax.swing.SpinnerNumberModel(640, 200, null, 1));
        width.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                widthvalueChanged(evt);
            }
        });

        height.setModel(new javax.swing.SpinnerNumberModel(360, 100, null, 1));
        height.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                heightvalueChanged(evt);
            }
        });

        jLabel4.setText("Height");

        jLabel5.setText("Shadowmap size");

        cFullscreen.setText("Fullscreen");
        cFullscreen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cFullscreen(evt);
            }
        });

        cShadows.setSelected(true);
        cShadows.setText("Shadows");
        cShadows.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setShadows(evt);
            }
        });

        jLabel6.setText("MSAA");

        msaa.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "4", "8", "16" }));
        msaa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                msaaChanged(evt);
            }
        });

        jLabel7.setText("Texture filtering");

        textureFiltering.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Bilinear", "Trilinear", "Anisotropic 2x", "Anisotropic 4x", "Anisotropic 8x", "Anisotropic 16x" }));
        textureFiltering.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textureFiltering(evt);
            }
        });

        vSync.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off", "60 fps", "30 fps" }));
        vSync.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                vSync(evt);
            }
        });

        jLabel9.setText("VSync");

        shadowMapSize.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "128", "256", "512", "1024", "2048", "4096" }));
        shadowMapSize.setSelectedIndex(4);
        shadowMapSize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shadowMapSize(evt);
            }
        });

        gammaLevel.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Off", "On" }));
        gammaLevel.setSelectedIndex(1);
        gammaLevel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gammaOnOff(evt);
            }
        });

        jLabel10.setText("Gamma correction");

        jLabel11.setText("Value");

        gammaValue.setModel(new javax.swing.SpinnerNumberModel(2.2f, null, null, 1.0f));
        gammaValue.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                gammaValue(evt);
            }
        });

        hdr.setText("Tone mapping");
        hdr.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                setHDR(evt);
            }
        });

        invert.setText("Invert");
        invert.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                invertActionPerformed(evt);
            }
        });

        grayscale.setText("Grayscale");
        grayscale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grayscaleActionPerformed(evt);
            }
        });

        fxaa.setText("FXAA");
        fxaa.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                fxaa(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cFrustumCulling)
                    .addComponent(cWireframeMode)
                    .addComponent(invert)
                    .addComponent(grayscale)
                    .addComponent(hdr, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fxaa))
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
                            .addComponent(msaa, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(vSync, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(textureFiltering, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cShadows, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(width, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gammaLevel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(height)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(shadowMapSize, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(cFullscreen, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addComponent(gammaValue, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))))
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
                                .addComponent(width, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(height, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cFullscreen))))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cShadows)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(shadowMapSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(21, 21, 21)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(msaa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textureFiltering, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(vSync, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(gammaLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gammaValue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(cWireframeMode)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cFrustumCulling)
                        .addGap(18, 18, 18)
                        .addComponent(invert)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(grayscale)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fxaa)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(hdr)))
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

    private void moved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_moved
        if (!Window.isFullscreen()) {
            Window.setPosition(new Vector2i(getX() + getWidth(), getY() + Window.getFrameSize().y));
        }
    }//GEN-LAST:event_moved

    private void widthvalueChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_widthvalueChanged
        dimensionChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_widthvalueChanged

    private void heightvalueChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_heightvalueChanged
        dimensionChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_heightvalueChanged

    private void shadowMapSize(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shadowMapSize
        Settings.setShadowMapResolution(Integer.valueOf(shadowMapSize.getItemAt(shadowMapSize.getSelectedIndex())));
    }//GEN-LAST:event_shadowMapSize

    private void vSync(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vSync
        vSyncChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_vSync

    private void exit(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exit
        Window.setWindowShouldClose(true);
    }//GEN-LAST:event_exit

    private void setWireframeMode(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setWireframeMode
        Settings.setWireframeMode(cWireframeMode.isSelected());
    }//GEN-LAST:event_setWireframeMode

    private void setFrustumCulling(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setFrustumCulling
        Settings.setFrustumCulling(cFrustumCulling.isSelected());
    }//GEN-LAST:event_setFrustumCulling

    private void setShadows(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setShadows
        Settings.setShadowMapping(cShadows.isSelected());
    }//GEN-LAST:event_setShadows

    private void textureFiltering(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textureFiltering
        textureFilteringChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_textureFiltering

    private void gammaValue(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_gammaValue
        gammaChanged = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_gammaValue

    private void setHDR(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_setHDR
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == ReinhardToneMappingRenderer.class) {
                renderer.setActive(hdr.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_setHDR

    private void msaaChanged(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_msaaChanged
        if (msaa.getSelectedIndex() == 0) {
            Settings.setMsaaLevel(1);
            return;
        }
        Settings.setMsaaLevel(2 << msaa.getSelectedIndex() - 1);
    }//GEN-LAST:event_msaaChanged

    private void gammaOnOff(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gammaOnOff
        gammaChanged = true;
        openGLRelatedSettingsChanged = true;
        gammaValue.setEnabled(gammaLevel.getSelectedIndex() == 1);
    }//GEN-LAST:event_gammaOnOff

    private void invertActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_invertActionPerformed
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == InvertRenderer.class) {
                renderer.setActive(invert.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_invertActionPerformed

    private void grayscaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grayscaleActionPerformed
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == GrayscaleRenderer.class) {
                renderer.setActive(grayscale.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_grayscaleActionPerformed

    private void fxaa(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_fxaa
        Renderer renderer;
        for (int i = 0; i < RenderingPipeline.getNumberOfRenderers(false); i++) {
            renderer = RenderingPipeline.getRenderer(false, i);
            if (renderer.getClass() == FxaaRenderer.class) {
                renderer.setActive(fxaa.isSelected());
                return;
            }
        }
    }//GEN-LAST:event_fxaa

    private void cFullscreen(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cFullscreen
        fullscreen = true;
        openGLRelatedSettingsChanged = true;
    }//GEN-LAST:event_cFullscreen

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cFrustumCulling;
    private javax.swing.JCheckBox cFullscreen;
    private javax.swing.JCheckBox cShadows;
    private javax.swing.JCheckBox cWireframeMode;
    private javax.swing.JCheckBox fxaa;
    private javax.swing.JComboBox<String> gammaLevel;
    private javax.swing.JSpinner gammaValue;
    private javax.swing.JCheckBox grayscale;
    private javax.swing.JCheckBox hdr;
    private javax.swing.JSpinner height;
    private javax.swing.JCheckBox invert;
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
    private javax.swing.JLabel lblFPS;
    private javax.swing.JLabel lblFboNumber;
    private javax.swing.JLabel lblMeshNumber;
    private javax.swing.JLabel lblMeshSize;
    private javax.swing.JLabel lblMeshSizeMega;
    private javax.swing.JLabel lblMilisec;
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
    private javax.swing.JComboBox<String> msaa;
    private javax.swing.JLabel renderedModels;
    private javax.swing.JLabel renderedTriangles;
    private javax.swing.JComboBox<String> shadowMapSize;
    private javax.swing.JComboBox<String> textureFiltering;
    private javax.swing.JComboBox<String> vSync;
    private javax.swing.JSpinner width;
    // End of variables declaration//GEN-END:variables

}
