package sonar.fluxnetworks.client.gui.tab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import sonar.fluxnetworks.api.gui.EnumFeedbackInfo;
import sonar.fluxnetworks.api.gui.EnumNavigationTabs;
import sonar.fluxnetworks.api.misc.FluxConstants;
import sonar.fluxnetworks.api.network.INetworkConnector;
import sonar.fluxnetworks.api.network.NetworkMember;
import sonar.fluxnetworks.api.text.FluxTranslate;
import sonar.fluxnetworks.client.FluxClientCache;
import sonar.fluxnetworks.client.gui.ScreenUtils;
import sonar.fluxnetworks.client.gui.basic.GuiTabPages;
import sonar.fluxnetworks.client.gui.button.InvisibleButton;
import sonar.fluxnetworks.client.gui.popups.PopUpUserEdit;
import sonar.fluxnetworks.common.handler.NetworkHandler;
import sonar.fluxnetworks.common.network.CGuiPermissionMessage;
import sonar.fluxnetworks.common.network.CNetworkUpdateMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class GuiTabMembers extends GuiTabPages<NetworkMember> {

    public InvisibleButton redirectButton;

    public NetworkMember selectedPlayer;

    private int timer;

    public GuiTabMembers(PlayerEntity player, INetworkConnector connector) {
        super(player, connector);
        gridStartX = 15;
        gridStartY = 22;
        gridHeight = 13;
        gridPerPage = 10;
        elementHeight = 12;
        elementWidth = 146;
        NetworkHandler.INSTANCE.sendToServer(new CNetworkUpdateMessage(network.getNetworkID(), FluxConstants.TYPE_NET_MEMBERS));
    }

    public EnumNavigationTabs getNavigationTab() {
        return EnumNavigationTabs.TAB_MEMBER;
    }

    @Override
    protected void drawForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        if (networkValid) {
            String str2 = accessPermission.getName();
            font.drawString(matrixStack, str2, 158 - font.getStringWidth(str2), 10, 0xffffff);
            font.drawString(matrixStack, FluxTranslate.SORT_BY.t() + ": " + TextFormatting.AQUA + FluxTranslate.SORTING_SMART.t(), 19, 10, 0xffffff);
            super.drawForegroundLayer(matrixStack, mouseX, mouseY);
        } else {
            super.drawForegroundLayer(matrixStack, mouseX, mouseY);
            renderNavigationPrompt(matrixStack, FluxTranslate.ERROR_NO_SELECTED.t(), FluxTranslate.TAB_SELECTION.t());
        }
    }

    @Override
    public void init() {
        /*if(networkValid) {

            buttons.add(new NormalButton("+", 152, 150, 12, 12, 1));

            player = TextboxButton.create(this, "", 1, fontRenderer, 14, 150, 130, 12);
            player.setMaxStringLength(32);

            textBoxes.add(player);
        }*/

        super.init();
        configureNavigationButtons(EnumNavigationTabs.TAB_MEMBER, navigationTabs);
        if (!networkValid) {
            redirectButton = new InvisibleButton(guiLeft + 20, guiTop + 16, 135, 20, EnumNavigationTabs.TAB_SELECTION.getTranslatedName(), b -> switchTab(EnumNavigationTabs.TAB_SELECTION, player, connector));
            addButton(redirectButton);
        }
    }

    @Override
    protected void onElementClicked(NetworkMember element, int mouseButton) {
        /*if(mouseButton == 0) {
            PacketHandler.network.sendToServer(new PacketGeneral.GeneralMessage(PacketGeneralType.CHANGE_PERMISSION, PacketGeneralHandler.getChangePermissionPacket(network.getNetworkID(), element.getPlayerUUID())));
        } else if(mouseButton == 1) {
            PacketHandler.network.sendToServer(new PacketGeneral.GeneralMessage(PacketGeneralType.REMOVE_MEMBER, PacketGeneralHandler.getRemoveMemberPacket(network.getNetworkID(), element.getPlayerUUID())));
        }*/
        if (mouseButton == 0) {
            selectedPlayer = element;
            openPopUp(new PopUpUserEdit(this, player, connector));
        }
    }

    @Override
    public void renderElement(MatrixStack matrixStack, NetworkMember element, int x, int y) {
        GlStateManager.enableBlend();
        GlStateManager.enableAlphaTest();

        int color = element.getPlayerAccess().color;

        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;

        RenderSystem.color4f(f, f1, f2, 0.8f);

        minecraft.getTextureManager().bindTexture(ScreenUtils.GUI_BAR);
        blit(matrixStack, x, y, 0, 16, elementWidth, elementHeight);

        if (element.getPlayerUUID().equals(player.getUniqueID())) {
            fill(matrixStack, x - 4, y + 1, x - 2, y + elementHeight - 1, 0xccffffff);
            fill(matrixStack, x + elementWidth + 2, y + 1, x + elementWidth + 4, y + elementHeight - 1, 0xccffffff);
        }

        font.drawString(matrixStack, TextFormatting.WHITE + element.getCachedName(), x + 4, y + 2, 0xffffff);

        String p = element.getPlayerAccess().getName();
        font.drawString(matrixStack, p, x + 142 - font.getStringWidth(p), y + 2, 0xffffff);
    }

    @Override
    public void renderElementTooltip(MatrixStack matrixStack, NetworkMember element, int mouseX, int mouseY) {
        if (hasActivePopup())
            return;
        List<String> strings = new ArrayList<>();
        strings.add(FluxTranslate.USERNAME.t() + ": " + TextFormatting.AQUA + element.getCachedName());
        String permission = element.getPlayerAccess().getName() + (element.getPlayerUUID().equals(player.getUniqueID()) ? " (" + FluxTranslate.YOU.t() + ")" : "");
        strings.add(FluxTranslate.ACCESS.t() + ": " + TextFormatting.RESET + permission);
        //strings.add(TextFormatting.GRAY + "UUID: " + TextFormatting.RESET + element.getPlayerUUID().toString());
        /*if(element.getPlayerUUID().equals(player.getUniqueID())) {
            strings.add(TextFormatting.WHITE + "You");
        }*/
        screenUtils.drawHoverTooltip(matrixStack, strings, mouseX + 4, mouseY - 8);
    }

    @Override
    public boolean mouseClickedMain(double mouseX, double mouseY, int mouseButton) {
        super.mouseClickedMain(mouseX, mouseY, mouseButton);
        /*for(NormalButton button : buttons) {
            if(button.isMouseHovered(mc, mouseX - guiLeft, mouseY - guiTop)) {
                if(button.id == 1 && !player.getText().isEmpty()) {
                    PacketHandler.network.sendToServer(new PacketGeneral.GeneralMessage(PacketGeneralType.ADD_MEMBER, PacketGeneralHandler.getAddMemberPacket(network.getNetworkID(), player.getText())));
                    player.setText("");
                }
            }
        }*/
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (timer == 0) {
            NetworkHandler.INSTANCE.sendToServer(new CGuiPermissionMessage(network.getNetworkID()));
        }
        if (timer % 2 == 0) {
            refreshPages(network.getMemberList());
            if (FluxClientCache.getFeedback(true) == EnumFeedbackInfo.SUCCESS) {
                if (hasActivePopup()) {
                    Optional<NetworkMember> n = elements.stream().filter(f -> f.getPlayerUUID().equals(selectedPlayer.getPlayerUUID())).findFirst();
                    if (n.isPresent()) {
                        selectedPlayer = n.get();
                        openPopUp(new PopUpUserEdit(this, player, connector));
                    } else {
                        closePopUp();
                    }
                }
                FluxClientCache.setFeedback(EnumFeedbackInfo.NONE, true);
            }
        }
        timer++;
        timer %= 40;
    }

    @Override
    protected void sortGrids(SortType sortType) {
        elements.sort(Comparator.comparing(NetworkMember::getPlayerAccess).thenComparing(NetworkMember::getCachedName));
        refreshCurrentPageInternal();
    }
}
