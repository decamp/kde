function data = testIdct1(data)
% computes the 1 dimensional inverse discrete cosine transform
[nrows,ncols]=size(data);
% Compute wieghts

w = exp(i*(0:nrows-1)*pi/(2*nrows)).';
weights=w(:,ones(1,ncols));

data=idct1d(data);
    function out=idct1d(x)
        y = real(ifft(weights.*x));
        out = zeros(nrows,ncols);
        out(1:2:nrows,:) = y(1:nrows/2,:);
        out(2:2:nrows,:) = y(nrows:-1:nrows/2+1,:);
    end
end
