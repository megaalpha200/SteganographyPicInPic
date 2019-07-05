% Encode Function
% The function asks the user to enter the location of the image they want 
% to hide (as a string), the location of the image they want to encode 
% (as a string), and the location of the new encoded image (as a string). 
% Finally, the function writes the new image into a file.

function encode()
    embeddedImgPath = input('Enter the location of the image you wish to embed: ', 's');
    originalPath = input('Enter the location of the image to be encoded: ', 's');
    newPath = input('Enter the location where you wish to save the new encoded image: ', 's');
    disp('Encoding Image...');
    
    %Read the image and produce an new encoded version
    img = imread(originalPath);
    imgToHide = imread(embeddedImgPath);
    newImg = embedMessage(img, imgToHide);
    
    %Write the image to a file
    imwrite(newImg, [newPath '.png']);
    movefile([newPath '.png'], newPath);
    
    sprintf('Image encoded successfully!\n');
end