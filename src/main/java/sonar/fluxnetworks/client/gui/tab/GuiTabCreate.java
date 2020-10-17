package sonar.fluxnetworks.client.gui.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import sonar.fluxnetworks.api.gui.EnumFeedbackInfo;
import sonar.fluxnetworks.api.gui.EnumNavigationTabs;
import sonar.fluxnetworks.api.gui.EnumNetworkColor;
import sonar.fluxnetworks.api.misc.EnergyType;
import sonar.fluxnetworks.api.network.INetworkConnector;
import sonar.fluxnetworks.api.network.NetworkSecurity;
import sonar.fluxnetworks.api.text.FluxTranslate;
import sonar.fluxnetworks.client.FluxClientCache;
import sonar.fluxnetworks.client.gui.basic.GuiButtonCore;
import sonar.fluxnetworks.client.gui.button.ColorButton;
import sonar.fluxnetworks.client.gui.button.NormalButton;
import sonar.fluxnetworks.common.handler.NetworkHandler;
import sonar.fluxnetworks.common.network.CCreateNetworkMessage;

public class GuiTabCreate extends GuiTabEditAbstract {

    public NormalButton apply, create;

    public GuiTabCreate(PlayerEntity player, INetworkConnector connector) {
        super(player, connector);
        securityType = NetworkSecurity.Type.ENCRYPTED;
        energyType = EnergyType.FE;
    }

    public EnumNavigationTabs getNavigationTab() {
        return EnumNavigationTabs.TAB_CREATE;
    }

    @Override
    public void init() {
        super.init();
        nameField.setText(minecraft.player.getDisplayName().getString() + "'s Network");
        int i = 0;
        for (EnumNetworkColor color : EnumNetworkColor.values()) {
            colorButtons.add(new ColorButton(48 + (i >= 7 ? i - 7 : i) * 16, 96 + (i >= 7 ? 1 : 0) * 16, color.getRGB()));
            i++;
        }
        colorBtn = colorButtons.get(0);
        colorBtn.selected = true;

        buttons.add(create = new NormalButton(FluxTranslate.CREATE.t(), 70, 150, 36, 12, 3).setUnclickable());
    }

    @Override
    protected void drawForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawForegroundLayer(matrixStack, mouseX, mouseY);

        screenUtils.renderNetwork(matrixStack, nameField.getText(), colorBtn.color, 20, 129);
        drawCenteredString(matrixStack, font, TextFormatting.RED + FluxClientCache.getFeedback(false).getInfo(), 88, 150, 0xffffff);
    }

    @Override
    public void onButtonClicked(GuiButtonCore button, int mouseX, int mouseY, int mouseButton) {
        super.onButtonClicked(button, mouseX, mouseY, mouseButton);
        if (button instanceof NormalButton) {
            if (mouseButton == 0 && button.id == 3) {
                //PacketHandler.CHANNEL.sendToServer(new GeneralPacket(GeneralPacketEnum.CREATE_NETWORK, GeneralPacketHandler.getCreateNetworkPacket(name.getText(), color.color, securityType, energyType, password.getText())));
                NetworkHandler.INSTANCE.sendToServer(new CCreateNetworkMessage(
                        nameField.getText(), colorBtn.color, securityType, passwordField.getText()));
            }
        }
    }

    @Override
    public void onEditSettingsChanged() {
        if (create != null) {
            create.clickable = (securityType != NetworkSecurity.Type.ENCRYPTED
                    || passwordField.getText().length() != 0) && nameField.getText().length() != 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (FluxClientCache.getFeedback(true) == EnumFeedbackInfo.SUCCESS) {
            switchTab(EnumNavigationTabs.TAB_SELECTION, player, connector);
            FluxClientCache.setFeedback(EnumFeedbackInfo.NONE, true);
        }
    }
}
