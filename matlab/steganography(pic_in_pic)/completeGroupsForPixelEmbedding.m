% Add Proper Padding for Pixel Embedding
% The function takes an int input. It returns a string output 
% represented in binary with propper padding according to the
% number of components in each pixel for which the string will
% be embedded into accordingly.

function paddedInput = completeGroupsForPixelEmbedding( input )

binInput = dec2bin(input);
[~, paddingAmt] = size(binInput);

paddingAmt = ceil(paddingAmt/4)*4;
bufferGrouping = getBufferGrouping();

overallGroupPadding = paddingAmt;
if (mod(overallGroupPadding, bufferGrouping) ~= 0)
    overallGroupPadding = (ceil(overallGroupPadding / getBufferGrouping()) + 1) * getBufferGrouping();
end

paddedBinInput = padString(binInput, '0', overallGroupPadding, 0);
paddedInput = paddedBinInput;
end

