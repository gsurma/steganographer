<h3 align="center">
  <img src="assets/steganographer_icon_web.png" width="300">
</h3>

# Steganographer

> Steganography is the practice of concealing a file, message, image, or video within another file, message, image, or video.

## About
Simple java script allowing hiding or revealing data in image files using Least Significant Bit algorithm.

## Example

### Before
![](Example/sample_image.png)

Original image
<br>
<br>

### After

![](Example/sample_image_with_hidden_message.png)

Image with the hidden message: `Very secret message!`


## How to use

### Encoding
1. Prepare a container image (preferably .png) 
2. Prepare a secret message
3. Encode `java Steganographer <path_to_container_image> <path_to_message_text_file>`
4. Steganographer should output an image file with hidden message 


### Decoding
1. Prepare an image with hidden message
2. Decode `java Steganographer <path_to_image_with_hidden_message>`
3. Steganographer should output a text file with hidden message
