% Decode Function
% The function takes a asks the user to enter the location of the image they 
% want to decode and where the user wants to save the retireved image. 
% Finally, the function writes the retrieved image into a file.

function decode()
    picPath = input('Enter the location of the encoded image: ', 's');
    retrievedImagePath = input('Enter the location where you wish to save the retrieved image: ', 's');
    disp('Retrieving Image...');
    
    encodedImg = imread(picPath);
    retImg = retrieveEncodedImageFromImage(encodedImg);
    
    %Write the image to a file
    imwrite(retImg, [retrievedImagePath '.png']);
    movefile([retrievedImagePath '.png'], retrievedImagePath);
    
    sprintf('Image decoded successfully!\n');
end