package me.dinozoid.strife.module.implementations.misc;

import com.sun.org.apache.xpath.internal.operations.Mod;
import com.wrapper.spotify.SpotifyApi;
import me.dinozoid.strife.Client;
import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.render.Render2DEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;

import com.wrapper.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.Track;
import me.dinozoid.strife.util.Dragging;
import me.dinozoid.strife.util.SpotifyAPI;
import me.dinozoid.strife.util.render.RenderUtil;
import me.dinozoid.strife.util.render.RoundedUtil;
import me.dinozoid.strife.util.render.StencilUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import static org.lwjgl.opengl.GL11.*;

@ModuleInfo(name = "Spotify", renderName = "Spotify", category = Category.MISC)
public class SpotifyModule extends Module {

    /**
     * PosX and PosY that will allow dragging & other stuff
     */
    private Dragging dragging = Client.INSTANCE.createDrag(this, "spotifyapi", 50, 50);
    //private int posX = 50, posY = 50;

    private SpotifyAPI spotifyAPI;
    private boolean downloadedCover;
    private Color imageColor = Color.WHITE;
    private ResourceLocation currentAlbumCover;
    
    private Track currentTrack;
    private CurrentlyPlayingContext currentPlayingContext;


    @EventHandler
    private final Listener<Render2DEvent> render2DEventListener = new Listener<>(event -> {
       renderEvent();
    });

    // Call this on your render event
    public void renderEvent() {
        Gui.drawRect(0, 0, 0, 0, 0);
        //If the user is not playing anything or if the user is not authenticated yet
        if (mc.thePlayer == null || spotifyAPI.currentTrack == null || spotifyAPI.currentPlayingContext == null) {
            return;
        }
        //If the current track does not equal the track that is playing on spotify then it sets the variable to the current track
        if (currentTrack != spotifyAPI.currentTrack || currentPlayingContext != spotifyAPI.currentPlayingContext) {
            this.currentTrack = spotifyAPI.currentTrack;
            this.currentPlayingContext = spotifyAPI.currentPlayingContext;
        }

        // You can make these two customizable.
        final int albumCoverSize = 40;
        final int playerWidth = 160;

        final int diff = currentTrack.getDurationMs() - currentPlayingContext.getProgress_ms();
        final long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        final long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        final String trackRemaining = String.format("-%s:%s", diffMinutes < 10 ? "0" + diffMinutes : diffMinutes, diffSeconds < 10 ? "0" + diffSeconds : diffSeconds);

        try {
            float posX = dragging.getX(), posY = dragging.getY();
            dragging.setWidth(playerWidth + 41);
            dragging.setHeight(albumCoverSize);
            // The rect methods that have WH at the end means they use width & height instead of x2 and y2

            //Gradient Rect behind the text
            glPushMatrix();
            RoundedUtil.drawGradientHorizontal(posX + albumCoverSize, posY + 1, playerWidth, albumCoverSize - 2f, 0, imageColor, new Color(20, 20, 20));
            RenderUtil.resetColor();
            glPopMatrix();
            //if (currentAlbumCover != null && downloadedCover) {
//                StencilUtil.initStencilToWrite();
//                RoundedUtil.drawRound(posX + albumCoverSize, posY, playerWidth, albumCoverSize, 4, false, Color.WHITE);
//                StencilUtil.readStencilBuffer(1);
//                RenderUtil.drawImageWithTint(currentAlbumCover, posX + albumCoverSize, posY, playerWidth, albumCoverSize, );
//                StencilUtil.uninitStencilBuffer();
//            }

            //We scissor the text to be inside the box
//            RenderUtil.makeCropBox(posX + albumCoverSize, posY, playerWidth, albumCoverSize);
//            glEnable(GL_SCISSOR_TEST);

            // Display the current track name
            // TODO: make the text of the current track and artist scroll back and forth, with a pause at each end.
            mc.fontRendererObj.drawString("§l" + currentTrack.getName(), posX + albumCoverSize + 4, posY + 6, -1);

            /*For every artist, append them to a string builder to make them into a single string
            They are separated by commas unless there is only one Or if its the last one, then its a dot.*/
            final StringBuilder artistsDisplay = new StringBuilder();
            for (int artistIndex = 0; artistIndex < currentTrack.getArtists().length; artistIndex++) {
                final ArtistSimplified artist = currentTrack.getArtists()[artistIndex];
                artistsDisplay.append(artist.getName()).append(artistIndex + 1 == currentTrack.getArtists().length ? '.' : ", ");
            }

            mc.fontRendererObj.drawString(artistsDisplay.toString(), posX + albumCoverSize + 4, posY + 17, -1);
           // RenderUtil.destroyCropBox();

            // Draw how much time until the track ends
            mc.fontRendererObj.drawString(trackRemaining, posX + playerWidth + 8, posY + albumCoverSize - 11, -1);

            //This is where we draw the progress bar
            final int progressBarWidth = ((playerWidth - albumCoverSize) * currentPlayingContext.getProgress_ms()) / currentTrack.getDurationMs();
            Gui.drawRect(posX + albumCoverSize + 5, posY + albumCoverSize - 9, posX + albumCoverSize + 5 + (playerWidth - albumCoverSize), (posY + albumCoverSize - 9) + 4, new Color(50, 50, 50).getRGB());
            Gui.drawRect(posX + albumCoverSize + 5, posY + albumCoverSize - 9, posX + albumCoverSize + 5 + progressBarWidth, (posY + albumCoverSize - 9) + 4, new Color(20, 200, 10).getRGB());

            if (currentAlbumCover != null && downloadedCover) {
                mc.getTextureManager().bindTexture(currentAlbumCover);
                GlStateManager.color(1,1,1);
                Gui.drawModalRectWithCustomSizedTexture(posX, posY, 0, 0, albumCoverSize, albumCoverSize, albumCoverSize, albumCoverSize);
            }

            if ((currentAlbumCover == null || !currentAlbumCover.getResourcePath().contains(currentTrack.getAlbum().getId()))) {
                downloadedCover = false;
                final ThreadDownloadImageData albumCover = new ThreadDownloadImageData(null, currentTrack.getAlbum().getImages()[1].getUrl(), null, new IImageBuffer() {
                    @Override
                    public BufferedImage parseUserSkin(BufferedImage image) {
                        imageColor = RenderUtil.getAverageColor(image, image.getWidth(), image.getHeight(), 1);
                        downloadedCover = true;
                        return image;
                    }

                    @Override
                    public void skinAvailable() {
                    }
                });
                GlStateManager.color(1, 1, 1);
                mc.getTextureManager().loadTexture(currentAlbumCover = new ResourceLocation("spotifyAlbums/" + currentTrack.getAlbum().getId()), albumCover);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onEnable() {
        if (mc.thePlayer == null) {
            toggle();
            return;
        }
        spotifyAPI.init();
        super.onEnable();
    }
}
