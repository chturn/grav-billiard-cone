module CubicSolver where

cubicRoot :: RealFloat a => a -> a -> a -> a -> a
cubicRoot a b c d = 2 * delta * sin phi + x0
  where x0 = -b / (3 * a)
        y0 = (2 * b^3) / (27 * a^2) - b * c / (3 * a) + d
        delta = sqrt $ (b^2 - 3 * a * c) / (9 * a^2)
        phi = (1/3) * (asin $ y0 / (2 * a * delta^3))
