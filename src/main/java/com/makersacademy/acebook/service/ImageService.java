package com.makersacademy.acebook.service;

import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class ImageService {

    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;
    private static final float JPEG_QUALITY = 0.7f;

    public byte[] compressImage(byte[] originalImage) throws IOException {

        BufferedImage original = ImageIO.read(
                new ByteArrayInputStream(originalImage)
        );

        if (original == null) {
            throw new IOException("Could not read image");
        }

        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double scale = Math.min(
                1.0,
                Math.min(
                        (double) MAX_WIDTH / originalWidth,
                        (double) MAX_HEIGHT / originalHeight
                )
        );

        int newWidth = (int) (originalWidth * scale);
        int newHeight = (int) (originalHeight * scale);

        BufferedImage resized = new BufferedImage(
                newWidth,
                newHeight,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = resized.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );

        graphics.drawImage(
                original,
                0,
                0,
                newWidth,
                newHeight,
                null
        );

        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();

        try (ImageOutputStream imageOutput =
                     ImageIO.createImageOutputStream(output)) {

            writer.setOutput(imageOutput);

            ImageWriteParam params = writer.getDefaultWriteParam();

            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(JPEG_QUALITY);

            writer.write(
                    null,
                    new IIOImage(resized, null, null),
                    params
            );

        } finally {
            writer.dispose();
        }

        return output.toByteArray();
    }
}