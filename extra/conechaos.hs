module ConeChaos where

import Graphics.Gloss
import GHC.Float
import CubicSolver

-- class Vec a where
--   vec :: (a,a,a)
             
-- instance Vec Position where
--   vec (Pos x y z) = (x,y,z)
--   vec (Cyl s p z) = (s,p,z)
--   vec (Sph r t p) = (r,t,p)

-- type Billiard = (Pos, Vel, Spn)

type Triple = (Float, Float, Float)
type Billiard = (Triple, Triple)
type Model = (Triple, Billiard)
type Data = (Model, Path)

main :: IO ()
main = simulate displayMode bgColor stepsPerSec initModel picFunc stepFunc
  where
    displayMode = InWindow "Grav-Billiard-Cone" (500,500) (50,20)
    bgColor = white
    stepsPerSec = 50
    initBil = ((0.6, 0, 0), (1, 0.23, 1))
    initModel = (((0.0, pi / 4, 5), initBil), [])
    fullModel = (iterate (updateData 0.01) initModel)!!5000
    picFunc = (translate (-100) (-100)).(picModel 300)
    stepFunc _ = updateData

picModel :: Float -> Data -> Picture
picModel h (((tp,theta,g), ((z,t,t'),(u,phi,vz))), path) = Pictures $ wedge:trace:[]
  where
    d = 5
    w = h * tan theta
    trace = Color red $ scale h h $ Line path
    wedge = Color black $ Polygon $ (0, 0):(w, h):(w, -d):(-d, -d):(-d, h):(0, h):[]    
--    bil = Color blue $ translate (z * w) (z * h) $ ThickCircle (d/2) d
--    time = translate 0 (-120) $ scale (1/5) (1/5) $ Text (show (z,t'))

-- square (x,y,z) = x^2 + y^2 + z^2

updateData :: Float -> Data -> Data
updateData dt d@(((tm,theta,g), ((z,tn,t'), (u, phi, vz))), path) = (updateModel dt (fst d), (x, y):path)
  where
    t = if (tm+dt > t') then t' - tn else (tm+dt - tn)
    p = z * tan theta
    x = sqrt $ u^2 * t^2 - 2 * t * u * p * cos phi + p^2
    y = z + vz * t - (1/2) * g * t^2
    
updateModel :: Float -> Model -> Model
updateModel dt ((t, theta, g), bil) = (params, updateBil params bil)
  where params = (t+dt, theta, g)

impactTime :: Triple -> Billiard -> Float
impactTime (_, theta, g) ((z, _, _), (u, phi, vz)) = cubicRoot a b c d
  where 
    a = g^2 / 4
    b = -g * vz
    c = vz^2 - g * z - (u / tan theta)^2
    d = 2 * z * (vz + (u * cos phi / tan theta))

updateBil :: Triple -> Billiard -> Billiard
updateBil p@(tm, _, _) b@((_, t, t'), _) | tm > t' = updateBil p (stepBil p b)
                                         | otherwise = b
                                         
stepBil :: Triple -> Billiard -> Billiard
stepBil p@(tm, _, g) b@((z, t, t'), (u, phi, vz)) = bounce p ((z', t', t'), (u', phi', v'z))
  where
    tau = t' - t
    z' = z + vz * tau - (1/2) * g * tau^2
    u' = u
    phi' = pi - (asin $ (z/z') * sin phi)
    v'z = vz - g * tau

bounce :: Triple -> Billiard -> Billiard
bounce p@(_, theta, _) ((z, t, _), vel) = ((z, t, t'), vel')
   where
    bil = ((z, 0, 0), vel')
    vel' = reflect theta vel
    t' = t + impactTime p bil

reflect :: Float -> Triple -> Triple
reflect theta (u, phi, vz) = (u', phi', v'z)
  where
    u' = sqrt (v'p^2 + v't^2)
    phi' = atan (v't/v'p)
    v'z = vr * cos theta - vn * sin theta
    v'p = -vr * sin theta - vn * cos theta
    v't = vt
    vr = vz * cos theta - u * cos phi * sin theta
    vn = vz * sin theta + u * cos phi * cos theta
    vt = u * sin phi
