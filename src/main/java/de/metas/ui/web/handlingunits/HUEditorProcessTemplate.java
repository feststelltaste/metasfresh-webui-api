package de.metas.ui.web.handlingunits;

import java.util.stream.Stream;

import org.adempiere.ad.dao.IQueryBL;
import org.adempiere.util.Services;

import de.metas.handlingunits.model.I_M_HU;
import de.metas.ui.web.handlingunits.HUEditorRowFilter.Select;
import de.metas.ui.web.process.adprocess.ViewBasedProcessTemplate;
import de.metas.ui.web.window.datatypes.DocumentIdsSelection;
import de.metas.util.stream.StreamUtils;
import lombok.NonNull;

/*
 * #%L
 * metasfresh-webui-api
 * %%
 * Copyright (C) 2017 metas GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/gpl-2.0.html>.
 * #L%
 */

/**
 * A {@link ViewBasedProcessTemplate} implementation template which add convenient functionalities around {@link HUEditorView}.
 *
 * @author metas-dev <dev@metasfresh.com>
 *
 */
public abstract class HUEditorProcessTemplate extends ViewBasedProcessTemplate
{
	protected final boolean isHUEditorView()
	{
		return isViewClass(HUEditorView.class);
	}

	@Override
	protected final HUEditorView getView()
	{
		return super.getView(HUEditorView.class);
	}

	@Override
	protected final HUEditorRow getSingleSelectedRow()
	{
		return HUEditorRow.cast(super.getSingleSelectedRow());
	}

	protected final Stream<HUEditorRow> streamSelectedRows(@NonNull final HUEditorRowFilter filter)
	{
		final DocumentIdsSelection selectedDocumentIds = getSelectedDocumentIds();
		if (selectedDocumentIds.isEmpty())
		{
			return Stream.empty();
		}

		return getView().streamByIds(filter.andOnlyRowIds(selectedDocumentIds));
	}

	protected final Stream<Integer> streamSelectedHUIds(@NonNull final Select select)
	{
		return streamSelectedHUIds(HUEditorRowFilter.select(select));
	}

	protected final Stream<Integer> streamSelectedHUIds(@NonNull final HUEditorRowFilter filter)
	{
		return streamSelectedRows(filter)
				.map(HUEditorRow::getM_HU_ID)
				.filter(huId -> huId > 0);
	}

	/**
	 * Gets <b>all</b> selected {@link HUEditorRow}s and loads the top level-HUs from those.
	 * I.e. this method does not rely on {@link HUEditorRow#isTopLevel()}, but checks the underlying HU.
	 *
	 * @param select
	 * @return
	 */
	protected final Stream<I_M_HU> streamSelectedHUs(@NonNull final Select select)
	{
		return streamSelectedHUs(HUEditorRowFilter.select(select));
	}

	protected final Stream<I_M_HU> streamSelectedHUs(@NonNull final HUEditorRowFilter filter)
	{
		final Stream<Integer> huIds = streamSelectedHUIds(filter);
		return StreamUtils.dice(huIds, 100)
				.flatMap(huIdsChunk -> Services.get(IQueryBL.class)
						.createQueryBuilder(I_M_HU.class)
						.addInArrayFilter(I_M_HU.COLUMNNAME_M_HU_ID, huIdsChunk)
						.create()
						.stream(I_M_HU.class));
	}
}
