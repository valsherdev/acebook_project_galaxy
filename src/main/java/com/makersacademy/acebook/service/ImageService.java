package com.makersacademy.acebook.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Service
public class ImageService {

    private static final int MAX_WIDTH = 1200;
    private static final int MAX_HEIGHT = 1200;

    private static final int MAX_INPUT_WIDTH = 8000;
    private static final int MAX_INPUT_HEIGHT = 8000;

    private static final long MAX_FILE_SIZE = 10L * 1024L * 1024L;

    private static final float JPEG_QUALITY = 0.7f;

    public byte[] compressImage(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IOException("Image is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("Image file is too large. Maximum size is 10 MB.");
        }

        try (InputStream inputStream = file.getInputStream();
             ImageInputStream imageInputStream =
                     ImageIO.createImageInputStream(inputStream)) {

            if (imageInputStream == null) {
                throw new IOException("Could not read image");
            }

            Iterator<ImageReader> readers =
                    ImageIO.getImageReaders(imageInputStream);

            if (!readers.hasNext()) {
                throw new IOException("Unsupported image format");
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInputStream, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width > MAX_INPUT_WIDTH || height > MAX_INPUT_HEIGHT) {
                    throw new IOException(
                            "Image dimensions are too large. Maximum is "
                                    + MAX_INPUT_WIDTH + "x"
                                    + MAX_INPUT_HEIGHT + " pixels."
                    );
                }

                BufferedImage original = reader.read(0);

                if (original == null) {
                    throw new IOException("Could not decode image");
                }

                double scale = Math.min(
                        1.0,
                        Math.min(
                                (double) MAX_WIDTH / width,
                                (double) MAX_HEIGHT / height
                        )
                );

                int newWidth = Math.max(1, (int) (width * scale));
                int newHeight = Math.max(1, (int) (height * scale));

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

                graphics.setRenderingHint(
                        RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY
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
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();

                Iterator<ImageWriter> writers =
                        ImageIO.getImageWritersByFormatName("jpg");

                if (!writers.hasNext()) {
                    throw new IOException("No JPEG writer available");
                }

                ImageWriter writer = writers.next();

                try {
                    ImageWriteParam writeParam = writer.getDefaultWriteParam();
                    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                    writeParam.setCompressionQuality(JPEG_QUALITY);

                    try (ImageOutputStream imageOutputStream =
                                 ImageIO.createImageOutputStream(output)) {

                        writer.setOutput(imageOutputStream);
                        writer.write(null,
                                new javax.imageio.IIOImage(resized, null, null),
                                writeParam);
                    }
                } finally {
                    writer.dispose();
                }
                return output.toByteArray();

            } finally {
                reader.dispose();
            }
        }
    }
}