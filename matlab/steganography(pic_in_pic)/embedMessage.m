% Embed an Image into another Image
% The function takes a 3x3 array which represents the image to be encoded and 
% a 3x3 array which represents the image to be hidden. The second image will 
% be encoded into the first image. It returns a 3x3 array which represents 
% the new encoded image.

function newImg = embedMessage(originalImg, picToHide)
    startDateTime = datetime('now', 'Format', 'dd-MM-yyyy HH:mm:ss.SSS');
    
    fileID = fopen('debugEn.txt', 'wt');

	bufferGrouping = getBufferGrouping();
	bufferDelimiter = getBufferDelimiter();
	padding = getPadding();
	delimiter = getDelimiter();

    [originalPicHeight, originalPicWidth, ~] = size(originalImg); %Get the original img height and width
    [picToHideHeight, picToHideWidth, ~] = size(picToHide); %Get the hidden img height and width
    originalPicPixelArea = originalPicHeight * originalPicWidth;
    picToHidePixelArea = picToHideHeight * picToHideWidth;
    
    newImg = originalImg; %Copy the original image to a new instance
    
    picToHidePixelAreaBin = completeGroupsForPixelEmbedding(picToHideWidth);
    picToHidePixelAreaBin = horzcat(picToHidePixelAreaBin, bufferDelimiter);
    picToHidePixelAreaBin = horzcat(picToHidePixelAreaBin, completeGroupsForPixelEmbedding(picToHideHeight));
    
    [~, picToHidePixelAreaBinCSize] = size(picToHidePixelAreaBin);
    picToHidePixelAreaBinGroupSize = picToHidePixelAreaBinCSize / 4;
    totalPixelsForBuffer = picToHidePixelAreaBinGroupSize / bufferGrouping;
    
    if picToHidePixelArea > (originalPicPixelArea - totalPixelsForBuffer)
        error('Original Picture too small!');
    end
    
    fprintf(fileID, 'Image in Binary...\n\n');
    
    pixelsLeftForBuffer = totalPixelsForBuffer;
    insertDelimiter = false;
    picToHideYIndex = 1;
    picToHideXIndex = -1;
    shouldEmbedPixel = true;
    isInnerLoopBroken = false;
    for yIndex = 1:originalPicHeight
        for xIndex = 1:originalPicWidth
            if shouldEmbedPixel
                %Get the corresponding RGB values for the current pixel in
                %originalImg and give proper padding
                originalPixelRed = padString(dec2bin(originalImg(yIndex, xIndex, 1)), '0', 8, 0);
                originalPixelGreen = padString(dec2bin(originalImg(yIndex, xIndex, 2)), '0', 8, 0);
                originalPixelBlue = padString(dec2bin(originalImg(yIndex, xIndex, 3)), '0', 8, 0);

                %Get the corresponding RGB values for the current pixel in
                %originalImg as strings for display
                originalPixelRedStr = [originalPixelRed(1:4) ' ' originalPixelRed(5:8)];
                originalPixelGreenStr = [originalPixelGreen(1:4) ' ' originalPixelRed(5:8)];
                originalPixelBlueStr = [originalPixelBlue(1:4) ' ' originalPixelRed(5:8)];
                originalImgRbgBinStr = [originalPixelRedStr ' | ' originalPixelGreenStr ' | ' originalPixelBlueStr];

                fprintf(fileID, 'Original Image Pixel (x:%d, y:%d): R - %s G - %s B - %s\n', xIndex, yIndex, originalPixelRedStr, originalPixelGreenStr, originalPixelBlueStr);
                fprintf(fileID, 'Original Image Pixel (Full): %s\n', originalImgRbgBinStr);

                %Initialize RGB variables for the current pixel in picToHide
                picToHidePixelRed = '';
                picToHidePixelGreen = '';
                picToHidePixelBlue = '';
                picToHidePixelRedStr = '';
                picToHidePixelGreenStr = '';
                picToHidePixelBlueStr = '';
                picToHideRbgBinStr = '';

                if pixelsLeftForBuffer > 0
                    %For inserting the dimensions buffer
                    startIndex = picToHidePixelAreaBinCSize - (pixelsLeftForBuffer * bufferGrouping * 4) + 1; %Index used to determin where in the string to start using bits for encoding

                    picToHidePixelRed = [picToHidePixelAreaBin(startIndex: startIndex + 3) padding];
                    startIndex = startIndex + 4; %Update startIndex
                    picToHidePixelGreen = [picToHidePixelAreaBin(startIndex: startIndex + 3) padding];
                    startIndex = startIndex + 4; %Update startIndex
                    picToHidePixelBlue = [picToHidePixelAreaBin(startIndex: startIndex + 3) padding];

                    fprintf(fileID, 'BUFFER\n');

                    pixelsLeftForBuffer = pixelsLeftForBuffer - 1;
                    if pixelsLeftForBuffer == 0
                        insertDelimiter = true;
                    end
                elseif insertDelimiter
                    %For inserting a delimiter
                    picToHidePixelRed = [delimiter(1:4) delimiter(1:4)];
                    picToHidePixelGreen = [delimiter(5:8) delimiter(5:8)];
                    picToHidePixelBlue = [delimiter(9:12) delimiter(9:12)];

                    fprintf(fileID, 'DELIMITER\n');
                    insertDelimiter = false;
                else
                    %Get the corresponding RGB values for the current pixel in
                    %picToHide and give proper padding
                    picToHidePixelRed = padString(dec2bin(picToHide(picToHideYIndex, picToHideXIndex, 1)), '0', 8, 0);
                    picToHidePixelGreen = padString(dec2bin(picToHide(picToHideYIndex, picToHideXIndex, 2)), '0', 8, 0);
                    picToHidePixelBlue = padString(dec2bin(picToHide(picToHideYIndex, picToHideXIndex, 3)), '0', 8, 0);
                end

                %Get the corresponding RGB values for the current pixel in
                %picToHide as strings for display
                picToHidePixelRedStr = [picToHidePixelRed(1:4) ' ' picToHidePixelRed(5:8)];
                picToHidePixelGreenStr = [picToHidePixelGreen(1:4) ' ' picToHidePixelGreen(5:8)];
                picToHidePixelBlueStr = [picToHidePixelBlue(1:4) ' ' picToHidePixelBlue(5:8)];
                picToHideRbgBinStr = [picToHidePixelRedStr ' | ' picToHidePixelGreenStr ' | ' picToHidePixelGreenStr];

                fprintf(fileID, 'Picture To Hide Image Pixel (x:%d, y:%d): R - %s G - %s B - %s\n', picToHideXIndex, picToHideYIndex, picToHidePixelRedStr, picToHidePixelGreenStr, picToHidePixelBlueStr);
                fprintf(fileID, 'Picture To Hide Image Pixel (Full): %s\n', picToHideRbgBinStr);

                %Construct the corresponding RGB values for the current pixel in
                %newImg and the strings for display
                newPixelRed = [originalPixelRed(1:4) picToHidePixelRed(1:4)];
                newPixelGreen = [originalPixelGreen(1:4) picToHidePixelGreen(1:4)];
                newPixelBlue = [originalPixelBlue(1:4) picToHidePixelBlue(1:4)];
                newPixelRedStr = [newPixelRed(1:4) ' ' newPixelRed(5:8)];
                newPixelGreenStr = [newPixelGreen(1:4) ' ' newPixelGreen(5:8)];
                newPixelBlueStr = [newPixelBlue(1:4) ' ' newPixelBlue(5:8)];
                newPixelRbgBinStr = [newPixelRedStr ' | ' newPixelGreenStr ' | ' newPixelBlueStr];

                fprintf(fileID, 'New Image Pixel (x:%d, y:%d): R - %s G - %s B - %s\n', xIndex, yIndex, newPixelRedStr, newPixelGreenStr, newPixelBlueStr);
                fprintf(fileID, 'New Image Pixel (Full): %s\n\n', newPixelRbgBinStr);
                
                %Replace the RGB values of the current pixel in newImg
                newImg(yIndex, xIndex, 1) = bin2dec(newPixelRed);
                newImg(yIndex, xIndex, 2) = bin2dec(newPixelGreen);
                newImg(yIndex, xIndex, 3) = bin2dec(newPixelBlue);
            else
%                 newImg(yIndex, xIndex, 1) = originalImg(yIndex, xIndex, 1);
%                 newImg(yIndex, xIndex, 2) = originalImg(yIndex, xIndex, 2);
%                 newImg(yIndex, xIndex, 3) = originalImg(yIndex, xIndex, 3);
                isInnerLoopBroken = true;
                break;
            end
            
            if (picToHideYIndex == picToHideHeight) && (picToHideXIndex == picToHideWidth)
                insertDelimiter = true;
                picToHideYIndex = -100;
            elseif picToHideYIndex < 0
                shouldEmbedPixel = false;
            end
            
            if picToHideXIndex == picToHideWidth
                picToHideXIndex = 1;
                picToHideYIndex = picToHideYIndex + 1;
            elseif pixelsLeftForBuffer == 0
                picToHideXIndex = picToHideXIndex + 1;
            end
        end
        
        if isInnerLoopBroken
            break;
        end
    end
    
    fclose(fileID);
    
    endDateTime = datetime('now', 'Format', 'dd-MM-yyyy HH:mm:ss.SSS');
    td = endDateTime - startDateTime;
    td.Format = 's';
    td = char(td);
    fprintf('\nEncoding Time: %s\n\n', td);
end