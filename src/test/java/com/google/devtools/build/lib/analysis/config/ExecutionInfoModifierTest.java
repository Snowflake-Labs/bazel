// Copyright 2018 The Bazel Authors. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.devtools.build.lib.analysis.config;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import com.google.devtools.build.lib.analysis.config.ExecutionInfoModifier.Converter;
import com.google.devtools.common.options.OptionsParsingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests {@link ExecutionInfoModifier}. */
@RunWith(JUnit4.class)
public class ExecutionInfoModifierTest {

  private final ExecutionInfoModifier.Converter converter = new Converter();

  @Test
  public void executionInfoModifier_empty() throws Exception {
    ExecutionInfoModifier modifier = converter.convert("");
    assertThat(modifier.matches("Anything")).isFalse();
  }

  @Test
  public void executionInfoModifier_singleAdd() throws Exception {
    ExecutionInfoModifier modifier = converter.convert("Genrule=+x");
    assertThat(modifier.matches("SomethingElse")).isFalse();
    assertModifierMatchesAndResults(modifier, "Genrule", ImmutableSet.of("x"));
  }

  @Test
  public void executionInfoModifier_singleRemove() throws Exception {
    ExecutionInfoModifier modifier = converter.convert("Genrule=-x");
    Map<String, String> info = new HashMap<>();
    info.put("x", "");

    modifier.apply("Genrule", info);

    assertThat(info).isEmpty();
  }

  @Test
  public void executionInfoModifier_multipleExpressions() throws Exception {
    ExecutionInfoModifier modifier = converter.convert("Genrule=+x,.*=+y,CppCompile=+z");
    assertModifierMatchesAndResults(modifier, "Genrule", ImmutableSet.of("x", "y"));
    assertModifierMatchesAndResults(modifier, "CppCompile", ImmutableSet.of("y", "z"));
    assertModifierMatchesAndResults(modifier, "GenericAction", ImmutableSet.of("y"));
  }

  @Test
  public void executionInfoModifier_multipleOptionsAdditive() throws Exception {
    var modifier1 =
        converter.convert(
            "Genrule=+x,CppCompile=-y1,GenericAction=+z,MergeLayers=+t,OtherAction=+o");
    var modifier2 =
        converter.convert(
            "Genrule=-x,CppCompile=+y1,CppCompile=+y2,GenericAction=+z,MergeLayers=+u");
    var modifier3 = converter.convert(".*=-t");

    var modifiers = ImmutableList.of(modifier1, modifier2, modifier3);
    assertModifierMatchesAndResults(modifiers, /* additive= */ true, "Genrule", ImmutableSet.of());
    assertModifierMatchesAndResults(
        modifiers, /* additive= */ true, "CppCompile", ImmutableSet.of("y1", "y2"));
    assertModifierMatchesAndResults(
        modifiers, /* additive= */ true, "GenericAction", ImmutableSet.of("z"));
    assertModifierMatchesAndResults(
        modifiers, /* additive= */ true, "MergeLayers", ImmutableSet.of("u"));
    assertModifierMatchesAndResults(
        modifiers, /* additive= */ true, "OtherAction", ImmutableSet.of("o"));
  }

  @Test
  public void executionInfoModifier_multipleOptionsNonAdditive() throws Exception {
    var modifier1 =
        converter.convert(
            "Genrule=+x,CppCompile=-y1,GenericAction=+z,MergeLayers=+t,OtherAction=+o");
    var modifier2 =
        converter.convert(
            "Genrule=-x,CppCompile=+y1,CppCompile=+y2,GenericAction=+z,MergeLayers=+u");
    var modifier3 = converter.convert(".*=-t");

    var modifiers1 = ImmutableList.of(modifier1, modifier2);

    assertModifierMatchesAndResults(
        modifiers1, /* additive= */ false, "Genrule", ImmutableSet.of());
    assertModifierMatchesAndResults(
        modifiers1, /* additive= */ false, "CppCompile", ImmutableSet.of("y1", "y2"));
    assertModifierMatchesAndResults(
        modifiers1, /* additive= */ false, "GenericAction", ImmutableSet.of("z"));
    assertModifierMatchesAndResults(
        modifiers1, /* additive= */ false, "MergeLayers", ImmutableSet.of("u"));
    assertThat(ExecutionInfoModifier.matches(modifiers1, false, "OtherAction")).isFalse();

    var modifiers2 = ImmutableList.of(modifier1, modifier2, modifier3);

    assertModifierMatchesAndResults(
        modifiers2, /* additive= */ false, "Genrule", ImmutableSet.of());
    assertModifierMatchesAndResults(
        modifiers2, /* additive= */ false, "CppCompile", ImmutableSet.of());
    assertModifierMatchesAndResults(
        modifiers2, /* additive= */ false, "GenericAction", ImmutableSet.of());
    assertModifierMatchesAndResults(
        modifiers2, /* additive= */ false, "MergeLayers", ImmutableSet.of());
    assertModifierMatchesAndResults(
        modifiers2, /* additive= */ false, "OtherAction", ImmutableSet.of());
  }

  @Test
  public void executionInfoModifier_invalidFormat_throws() throws Exception {
    List<String> invalidModifiers =
        ImmutableList.of("A", "=", "A=", "A=+", "=+", "A=-B,A", "A=B", "A", ",");
    for (String invalidModifer : invalidModifiers) {
      assertThrows(OptionsParsingException.class, () -> converter.convert(invalidModifer));
    }
  }

  @Test
  public void executionInfoModifier_invalidFormat_exceptionShowsOffender() throws Exception {
    OptionsParsingException thrown =
        assertThrows(OptionsParsingException.class, () -> converter.convert("A=+1,B=2,C=-3"));
    assertThat(thrown).hasMessageThat().contains("malformed");
    assertThat(thrown).hasMessageThat().contains("'B=2'");
  }

  @Test
  public void executionInfoModifier_EqualsTester() throws Exception {
    new EqualsTester()
        // base empty
        .addEqualityGroup(converter.convert(""), converter.convert(""))
        // base non-empty
        .addEqualityGroup(converter.convert("A=+B"), converter.convert("A=+B"))
        // different pattern and key
        .addEqualityGroup(converter.convert("C=+D"))
        // different key
        .addEqualityGroup(converter.convert("A=+D"))
        // different pattern
        .addEqualityGroup(converter.convert("C=+B"))
        // different operation
        .addEqualityGroup(converter.convert("A=-B"))
        // more items
        .addEqualityGroup(converter.convert("A=+B,C=-D"), converter.convert("A=+B,C=-D"))
        // different order
        .addEqualityGroup(converter.convert("C=-D,A=+B"))
        .testEquals();
  }

  private void assertModifierMatchesAndResults(
      ExecutionInfoModifier modifier, String mnemonic, Set<String> expectedKeys) {
    assertModifierMatchesAndResults(
        ImmutableList.of(modifier), /* additive= */ false, mnemonic, expectedKeys);
  }

  private void assertModifierMatchesAndResults(
      List<ExecutionInfoModifier> modifiers,
      boolean additive,
      String mnemonic,
      Set<String> expectedKeys) {
    Map<String, String> copy = new HashMap<>();
    ExecutionInfoModifier.apply(modifiers, additive, mnemonic, copy);
    assertThat(ExecutionInfoModifier.matches(modifiers, additive, mnemonic)).isTrue();
    assertThat(copy)
        .containsExactlyEntriesIn(
            expectedKeys.stream().collect(ImmutableMap.toImmutableMap(k -> k, unused -> "")));
  }
}
