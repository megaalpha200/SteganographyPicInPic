%% 
% .
% Add Padding to a String
% The function takes a string input, a character input (used for padding), 
% a limit integer value (the total number of characters in the string), and 
% an integer that indicates the padding location (0 = Left; 1 = Right).
%The function resturns the padded string.
function paddedStr = padString( str, character, limit, direction )

    [~, cSize] = size(str);

    if cSize < limit
        tempStr = char(ones(1, limit) * character);
        
        if (direction == 0)
            tempStr(end-(cSize-1):end) = str; %Left Padding
        elseif (direction == 1)
            tempStr(1:cSize) = str; %Right Padding
        end
    else
        tempStr = str;
    end

    paddedStr = tempStr;
end
