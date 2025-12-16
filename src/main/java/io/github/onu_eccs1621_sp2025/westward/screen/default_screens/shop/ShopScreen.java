package io.github.onu_eccs1621_sp2025.westward.screen.default_screens.shop;

import com.google.gson.annotations.Expose;
import imgui.ImColor;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import io.github.onu_eccs1621_sp2025.westward.TrailApplication;
import io.github.onu_eccs1621_sp2025.westward.data.Audio;
import io.github.onu_eccs1621_sp2025.westward.data.ShopListing;
import io.github.onu_eccs1621_sp2025.westward.game.Game;
import io.github.onu_eccs1621_sp2025.westward.screen.Screen;
import io.github.onu_eccs1621_sp2025.westward.utils.rendering.RenderUtils;
import io.github.onu_eccs1621_sp2025.westward.utils.sound.SoundEngine;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Format;
import io.github.onu_eccs1621_sp2025.westward.utils.text.Translations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The screen where players buy supplies.
 * @author Dylan Catte, Ben Westover, Noah Sumerauer, Micah Lee
 * @since 1.0.0 Alpha 1
 * @version 1.1
 */
public class ShopScreen extends Screen {
    @Expose
    private final List<ShopListing> listings;
    private final HashMap<ShopListing, ImInt> inCart = new HashMap<>();

    /**
     * Creates the item shop.
     * @param data The data for the shop
     */
    public ShopScreen(ShopScreenData data) {
        super(data.id());
        this.listings = data.items();
    }

    @Override
    public void render() {
        // Shopping List
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, ImColor.rgb(50, 50, 50));
        ImVec2 cartSize = RenderUtils.getCursorRelative(0.4F, 0.97F);
        ImVec2 cartPos = RenderUtils.getCursorRelative(0.02F, 0.02F);
        ImGui.setCursorPos(cartPos);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 8.0F);
        ImGui.beginChild("cart", cartSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        ImGui.setCursorPos(cartSize.x / 2.0F - ImGui.calcTextSizeX(Translations.getTranslatedText("shop.cart")) / 2.0F, RenderUtils.getCursorRelative(0, 0.02F).y);
        ImGui.text(Translations.getTranslatedText("shop.cart"));
        Format.clearFontSize();

        Format.setFontSize(Format.MENU_FONT_SIZE);
        ImVec2 innerShoppingListPos = new ImVec2(cartSize.x * 0.02F, RenderUtils.getCursorRelative(0.0F, 0.1F).y);
        ImGui.setCursorPos(innerShoppingListPos);
        ImVec2 cartInnerSize = new ImVec2(cartSize.x, cartSize.y * 0.7F);
        ImGui.beginChild("cartInner", cartInnerSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        int i = 0;
        float totalPrice = 0.0F;
        Iterator<ShopListing> iterator = this.inCart.keySet().iterator();
        while (iterator.hasNext()) {
            totalPrice += renderPurchasedItemListing(iterator, i, cartInnerSize.x);
            i++;
        }
        ImGui.endChild();

        ImVec2 totalPos = new ImVec2(cartSize.x * 0.02F, RenderUtils.getCursorRelative(0.0F, 0.8F).y);
        ImGui.setCursorPos(totalPos);
        ImGui.text(Translations.getTranslatedText("shop.total"));
        ImVec2 totalPosAmount = new ImVec2(cartSize.x * 0.5F, RenderUtils.getCursorRelative(0.0F, 0.8F).y);
        ImGui.setCursorPos(totalPosAmount);
        ImGui.text("$" + String.format("%.2f", totalPrice));
        ImVec2 remainingMoneyPos = new ImVec2(cartSize.x * 0.02F, RenderUtils.getCursorRelative(0.0F, 0.84F).y);
        ImGui.setCursorPos(remainingMoneyPos);
        ImGui.text(Translations.getTranslatedText("shop.balance"));
        ImVec2 remainingMoneyAmountPos = new ImVec2(cartSize.x * 0.5F, RenderUtils.getCursorRelative(0.0F, 0.84F).y);
        ImGui.setCursorPos(remainingMoneyAmountPos);
        if (Game.getInstance().getMoney() - totalPrice < 0) {
            ImGui.textColored(ImColor.rgb(255, 0, 0), "$" + String.format("%.2f", Game.getInstance().getMoney() - totalPrice));
        } else {
            ImGui.text("$" + String.format("%.2f", Game.getInstance().getMoney() - totalPrice));
        }

        ImVec2 purchaseButtonSize = new ImVec2(cartSize.x * 0.5F, cartSize.y * 0.04F);
        ImGui.setCursorPos((float) (cartSize.x * 0.5 - (purchaseButtonSize.x / 2.0F)), RenderUtils.getCursorRelative(0.0F, 0.9F).y);
        String translationKey = totalPrice == 0.0F ? "shop.exit" : "shop.buy";
        if (ImGui.button(Translations.getTranslatedText(translationKey), purchaseButtonSize)) {
            this.purchase(totalPrice);
        }
        ImGui.endChild();
        Format.clearFontSize();

        // Shop
        Format.setFontSize(Format.TITLE_FONT_SIZE);
        ImVec2 shopSize = RenderUtils.getCursorRelative(0.53F, 0.97F);
        ImVec2 shopPos = RenderUtils.getCursorRelative(0.45F, 0.02F);
        ImGui.setCursorPos(shopPos);
        ImGui.beginChild("shop", shopSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        float width = shopSize.x;
        ImGui.setCursorPos(width / 2.0F - ImGui.calcTextSizeX(Translations.getTranslatedText("shop.offers")) / 2.0F, RenderUtils.getCursorRelative(0, 0.02F).y);
        ImGui.text(Translations.getTranslatedText("shop.offers"));
        Format.clearFontSize();

        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
        ImVec2 innerShopPos = new ImVec2(shopSize.x * 0.02F, RenderUtils.getCursorRelative(0.0F, 0.1F).y);
        ImGui.setCursorPos(innerShopPos);
        ImVec2 shopInnerSize = new ImVec2(shopSize.x, shopSize.y * 0.88F);
        ImGui.beginChild("shopInner", shopInnerSize, false, ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove);
        for (int j = 0; j < this.listings.size(); j++) {
            renderItemForSale(this.listings.get(j), j, shopInnerSize.x);
        }
        ImGui.endChild();

        ImGui.endChild();
        ImGui.popStyleColor();
        ImGui.popStyleVar();
        Format.clearFontSize();
    }

    private float renderPurchasedItemListing(Iterator<ShopListing> iterator, int index, float width) {
        ShopListing listing = iterator.next();
        ImVec2 listPosStart = new ImVec2(0.0F, RenderUtils.getCursorRelative(0, 0.1F + (0.05F * index)).y);
        ImVec2 listPosEnd = new ImVec2(width, RenderUtils.getCursorRelative(0, 0.05F + (0.05F * index)).y);

        float centerY = (listPosStart.y - listPosEnd.y) / 2.0F + listPosEnd.y;
        ImGui.setCursorPos(width * 0.02F, centerY);
        ImGui.text(Translations.getTranslatedText(listing.itemId()));
        ImGui.setCursorPos(width * 0.45F, centerY);
        ImGui.setNextItemWidth(width * 0.3F);
        if (ImGui.inputInt("##Quantity" + index, this.inCart.get(listing), 1, 5)) {
            if (this.inCart.get(listing).get() <= 0) {
                iterator.remove();
                return 0.0F;
            }
        }
        float totalCost = listing.price() * this.inCart.get(listing).get();
        ImGui.setCursorPos(width * 0.78F, centerY);
        ImGui.text("$" + String.format("%.2f", totalCost));
        return totalCost;
    }

    private void renderItemForSale(ShopListing listing, int index, float width) {
        ImVec2 listPosStart = new ImVec2(0.0F, RenderUtils.getCursorRelative(0, 0.1F + (0.05F * index)).y);
        ImVec2 listPosEnd = new ImVec2(width, RenderUtils.getCursorRelative(0, 0.05F + (0.05F * index)).y);

        float centerY = (listPosStart.y - listPosEnd.y) / 2.0F + listPosEnd.y;
        ImGui.setCursorPos(width * 0.05F, centerY);
        ImGui.text(Translations.getTranslatedText(listing.itemId()));
        ImGui.setCursorPos(width * 0.5F, centerY);
        ImGui.text(Translations.getTranslatedText("shop.price"));
        ImGui.setCursorPos(width * 0.7F, centerY);
        ImGui.text("$" + String.format("%.2f", listing.price()));
        float textHeight = ImGui.calcTextSizeY("0");
        ImGui.setCursorPos(width * 0.85F, centerY);
        ImVec2 addButtonSize = new ImVec2(width * 0.1F, textHeight);
        Format.clearFontSize();
        Format.setFontSize(Format.MENU_FONT_SIZE);
        if (ImGui.button(Translations.getTranslatedText("shop.add") + "##" + index, addButtonSize)) {
            SoundEngine.loadSFX(Audio.CLICK_1);
            if (this.inCart.containsKey(listing)) {
                this.inCart.get(listing).set(this.inCart.get(listing).get() + 1);
            } else {
                this.inCart.put(listing, new ImInt(1));
            }
        }
        Format.clearFontSize();
        Format.setFontSize(Format.DESCRIPTION_FONT_SIZE);
    }

    private void purchase(float totalCost) {
        if (totalCost > Game.getInstance().getMoney()) {
            return;
        }
        for (Map.Entry<ShopListing, ImInt> entry : this.inCart.entrySet()) {
            entry.getKey().purchase(entry.getValue().shortValue());
        }
        this.inCart.clear();
        this.close();
    }

    @Override
    public void close() {
        super.close();
        SoundEngine.loadSFX(Audio.SHOP_BELL);
    }
}
