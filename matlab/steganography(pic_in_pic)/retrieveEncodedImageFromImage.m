% Retrieve the Hidden Picture from another Picture
% The function takes a 3x3 array which represents the encoded image. It 
% returns a 3x3 array which is the hidden image.

function retImg = retrieveEncodedImageFromImage(encodedImg)
    bufferGrouping = getBufferGrouping();
	bufferDelimiter = getBufferDelimiter();
	padding = getPadding();
	delimiter = getDelimiter();

    retrievedImgHeight = 100;
    retrievedImgWidth = 100;
    
    [encodedImgHeight, encodedImgWidth, ~] = size(encodedImg);
    
    fileID = fopen('debugRET.txt', 'wt');
    
    bufferRetrieved = false;
    retrievedBuffer = '';
    retrievedImgYIndex = 1;
    retrievedImgXIndex = 1;
    innerLoopBroken = false;
    
    for yIndex = 1:encodedImgHeight
        for xIndex = 1:encodedImgWidth
            %Get the corresponding RGB values for the current pixel in
            %encodedImg and give proper padding
            encodedImgPixelRed = padString(dec2bin(encodedImg(yIndex, xIndex, 1)), '0', 8, 0);
            encodedImgPixelGreen = padString(dec2bin(encodedImg(yIndex, xIndex, 2)), '0', 8, 0);
            encodedImgPixelBlue = padString(dec2bin(encodedImg(yIndex, xIndex, 3)), '0', 8, 0);
            
            %Get the corresponding RGB values for the current pixel in
            %encodedImg as strings for display
            encodedImgPixelRedStr = [encodedImgPixelRed(1:4) ' ' encodedImgPixelRed(5:8)];
            encodedImgPixelGreenStr = [encodedImgPixelGreen(1:4) ' ' encodedImgPixelGreen(5:8)];
            encodedImgPixelBlueStr = [encodedImgPixelBlue(1:4) ' ' encodedImgPixelBlue(5:8)];
            encodedImgRbgBinStr = [encodedImgPixelRedStr ' | ' encodedImgPixelGreenStr ' | ' encodedImgPixelBlueStr];
            
            fprintf(fileID, 'Encoded Image Pixel (x:%d, y:%d): R - %s G - %s B - %s\n', xIndex, yIndex, encodedImgPixelRedStr, encodedImgPixelGreenStr, encodedImgPixelBlueStr);
            fprintf(fileID, 'Encoded Image Pixel (Full): %s\n', encodedImgRbgBinStr);
            
            retrievedBin = [encodedImgPixelRed(5:8) encodedImgPixelGreen(5:8) encodedImgPixelBlue(5:8)]; %Represents the encoded portion of the current pixel
            
            if ~bufferRetrieved %The buffer still needs to be retrieved
                if retrievedBin == delimiter
                    %The delimiter separating the buffer from the hidden
                    %image has been hit
                    bufferRetrieved = true;
                    retrievedImgHeight = bin2dec(retrievedBuffer);
                    
                    fprintf(fileID, 'Width: %d, Height: %d\n\n', retrievedImgWidth, retrievedImgHeight);
                    retImg = zeros(retrievedImgHeight, retrievedImgWidth, 3, 'uint8');
                elseif retrievedBin == bufferDelimiter
                    %The delimiter within the buffer itself has been hit
                    retrievedImgWidth = bin2dec(retrievedBuffer);
                    retrievedBuffer = '';
                else
                    %No delimiter has been hit yet; Append the contents of
                    %the retrieved encoded data to the retrievedBuffer
                    retrievedBuffer = horzcat(retrievedBuffer, retrievedBin);
                end
            else %The buffer is retrieved; Retreived the hidden image
                if retrievedBin == delimiter
                    %The final delimiter has been hit; Stop reading the
                    %encoded image
                    innerLoopBroken = true;
                    break;
                else
                    %Construct the corresponding RGB values for the current pixel in
                    %retImg and the strings for display
                    retrievedImgPixelRed = [encodedImgPixelRed(5:8) padding];
                    retrievedImgPixelGreen = [encodedImgPixelGreen(5:8) padding];
                    retrievedImgPixelBlue = [encodedImgPixelBlue(5:8) padding];
                    retrievedImgPixelRedStr = [retrievedImgPixelRed(1:4) ' ' retrievedImgPixelRed(5:8)];
                    retrievedImgPixelGreenStr = [retrievedImgPixelGreen(1:4) ' ' retrievedImgPixelGreen(5:8)];
                    retrievedImgPixelBlueStr = [retrievedImgPixelBlue(1:4) ' ' retrievedImgPixelBlue(5:8)];
                    retrievedImgRgbBinStr = [retrievedImgPixelRedStr ' | ' retrievedImgPixelGreenStr ' | ' retrievedImgPixelBlueStr];
                    
                    fprintf(fileID, 'Retrieved Image Pixel (x:%d, y:%d): R - %s G - %s B - %s\n', retrievedImgXIndex, retrievedImgYIndex, retrievedImgPixelRedStr, retrievedImgPixelGreenStr, retrievedImgPixelBlueStr);
                    fprintf(fileID, 'Retrieved Image Pixel (Full): %s\n\n', retrievedImgRgbBinStr);
                    
                    %Replace the RGB values of the current pixel in retImg
                    retImg(retrievedImgYIndex, retrievedImgXIndex, 1) = bin2dec(retrievedImgPixelRed);
                    retImg(retrievedImgYIndex, retrievedImgXIndex, 2) = bin2dec(retrievedImgPixelGreen);
                    retImg(retrievedImgYIndex, retrievedImgXIndex, 3) = bin2dec(retrievedImgPixelBlue);
                end
                
                if retrievedImgXIndex == retrievedImgWidth
                    retrievedImgXIndex = 1;
                    retrievedImgYIndex = retrievedImgYIndex + 1;
                else
                    retrievedImgXIndex = retrievedImgXIndex + 1;
                end
            end
        end
        
        if innerLoopBroken
            break;
        end
    end
    
    fclose(fileID);
end