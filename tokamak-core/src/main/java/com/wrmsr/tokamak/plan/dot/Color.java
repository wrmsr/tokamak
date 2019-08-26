/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wrmsr.tokamak.plan.dot;

import com.wrmsr.tokamak.util.lazy.CtorLazyValue;

import javax.annotation.concurrent.Immutable;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

@Immutable
final class Color
{
    private final int r;
    private final int g;
    private final int b;

    private final String string;

    public Color(int r, int g, int b)
    {
        for (int c : new int[] {r, g, b}) {
            checkArgument(c >= 0 && c < 256);
        }
        this.r = r;
        this.g = g;
        this.b = b;
        string = String.format("#%02x%02x%02x", r, g, b);
    }

    public int getR()
    {
        return r;
    }

    public int getG()
    {
        return g;
    }

    public int getB()
    {
        return b;
    }

    @Override
    public String toString()
    {
        return string;
    }

    private static final int RAINBOW_STEPS = 25;

    public static final CtorLazyValue<List<Color>> RAINBOW = new CtorLazyValue<>(() -> {
        List<Color> colors = new ArrayList<>();
        for (int r = 0; r < RAINBOW_STEPS; r++) { colors.add(new Color(r * 255 / RAINBOW_STEPS, 255, 0)); }
        for (int g = RAINBOW_STEPS; g > 0; g--) { colors.add(new Color(255, g * 255 / RAINBOW_STEPS, 0)); }
        for (int b = 0; b < RAINBOW_STEPS; b++) { colors.add(new Color(255, 0, b * 255 / RAINBOW_STEPS)); }
        for (int r = RAINBOW_STEPS; r > 0; r--) { colors.add(new Color(r * 255 / RAINBOW_STEPS, 0, 255)); }
        for (int g = 0; g < RAINBOW_STEPS; g++) { colors.add(new Color(0, g * 255 / RAINBOW_STEPS, 255)); }
        for (int b = RAINBOW_STEPS; b > 0; b--) { colors.add(new Color(0, 255, b * 255 / RAINBOW_STEPS)); }
        colors.add(new Color(0, 255, 0));
        return colors;
    });
}
