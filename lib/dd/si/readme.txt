Security Images  (captcha)
---------------

Author:		A.D.Surrey
Website:	www.surneo.com
Licence:	GPL (Free, use it for anything u wish)

This class implements security images in PHP using GD. 
(GD comes with PHP4).
It also uses custom TTF fronts and background images.

Changes
-------

version 1.3

Text is no longer all in the same orientation, that is each letter
will have a slightly different angle, vertical placement and size randomly chosen.

--------------------------------------------------------------------------

version 1.2

Added an example using the Captcha class with a Contact form

Added more Background Images and Fonts

Replaced rand() with mt_rand()
This just speeds things up a little ,it's essentially the same function.

--------------------------------------------------------------------------

version 1.1

Added a function to create extra "noise" or random dots 
to the image.

---------------------------------------------------------------------------